package com.rioni.lk.api.service.impl;

import com.rioni.lk.api.dto.CurrencyRateDto;
import com.rioni.lk.api.dto.CurrencyRatesResponse;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NbgCurrencyServiceImplTest {

    private static final ZoneId TBILISI = ZoneId.of("Asia/Tbilisi");

    /**
     * Mirrors the real NBG RSS structure: the first item's title contains the
     * rates date and the description holds an HTML table (inside CDATA).
     */
    private static final String SAMPLE_RSS = """
            <?xml version="1.0" encoding="UTF-8" ?>
            <rss version="2.0">
            <channel>
            <title>RSS NBG Currency Rates</title>
            <item>
             <title>Currency Rates 2026-08-05</title>
             <link>https://nbg.gov.ge</link>
             <description><![CDATA[<table border="0"><tr>
                    <td>AED</td><td>10 United Arab Emirates Dirhams</td><td>7.1438</td>
                    <td><img  src="https://www.nbg.gov.ge/images/green.gif"></td><td>0.0027</td>
                </tr><tr>
                    <td>EUR</td><td>1 Euro</td><td>3.0219</td>
                    <td><img  src="https://www.nbg.gov.ge/images/green.gif"></td><td>0.0020</td>
                </tr><tr>
                    <td>TRY</td><td>1 Turkish Lira</td><td>0.1200</td>
                    <td><img  src="https://www.nbg.gov.ge/images/red.gif"></td><td>0.0005</td>
                </tr><tr>
                    <td>USD</td><td>1 US Dollar</td><td>2.7000</td>
                    <td><img  src="https://www.nbg.gov.ge/images/green.gif"></td><td>0.0010</td>
                </tr><tr>
                    <td>ZAR</td><td>1 South African Rand</td><td>0.1500</td>
                    <td><img  src="https://www.nbg.gov.ge/images/red.gif"></td><td>0.0002</td>
                </tr></table>]]></description>
            </item>
            </channel>
            </rss>
            """;

    private final NbgCurrencyServiceImpl service = new NbgCurrencyServiceImpl("https://example.test/rss");

    // ─────────────────────────────────────────────────────────────────────────────
    //  RSS parsing
    // ─────────────────────────────────────────────────────────────────────────────

    @Test
    void parseRates_shouldExtractRssDate() throws Exception {
        CurrencyRatesResponse response = invokeParseRates(parseXml(SAMPLE_RSS));

        assertThat(response.getRss_date()).isEqualTo("2026-08-05");
    }

    @Test
    void parseRates_shouldParseRateFieldsAndDirectionFromIcon() throws Exception {
        CurrencyRatesResponse response = invokeParseRates(parseXml(SAMPLE_RSS));

        CurrencyRateDto usd = response.getRates().stream()
                .filter(r -> r.getCode().equals("USD"))
                .findFirst()
                .orElseThrow();

        assertThat(usd.getRate()).isEqualTo("2.7000");
        assertThat(usd.getChange()).isEqualTo("0.0010");
        assertThat(usd.getDirection()).isEqualTo("up");
        assertThat(usd.getIcon()).isEqualTo("▲");

        CurrencyRateDto tryLira = response.getRates().stream()
                .filter(r -> r.getCode().equals("TRY"))
                .findFirst()
                .orElseThrow();

        assertThat(tryLira.getDirection()).isEqualTo("down");
        assertThat(tryLira.getIcon()).isEqualTo("▼");
    }

    @Test
    void parseRates_shouldPutPriorityCurrenciesFirstThenAlphabetically() throws Exception {
        CurrencyRatesResponse response = invokeParseRates(parseXml(SAMPLE_RSS));

        // Priority order: USD, EUR, ..., TRY; then the rest alphabetically (AED, ZAR)
        assertThat(response.getRates()).extracting(CurrencyRateDto::getCode)
                .containsExactly("USD", "EUR", "TRY", "AED", "ZAR");
    }

    @Test
    void parseRates_withoutDescription_shouldReturnEmptyRatesWithDate() throws Exception {
        String rss = """
                <?xml version="1.0" encoding="UTF-8" ?>
                <rss version="2.0">
                <channel>
                <item>
                 <title>Currency Rates 2026-08-05</title>
                 <description></description>
                </item>
                </channel>
                </rss>
                """;

        CurrencyRatesResponse response = invokeParseRates(parseXml(rss));

        assertThat(response.getRss_date()).isEqualTo("2026-08-05");
        assertThat(response.getRates()).isEmpty();
    }

    // ─────────────────────────────────────────────────────────────────────────────
    //  Cache window meta
    // ─────────────────────────────────────────────────────────────────────────────

    @Test
    void cacheWindowMeta_insideUpdateWindow_shouldUse900sTtl() throws Exception {
        // 2026-08-04 17:00 Tbilisi is inside the 16:00-19:00 update window
        long now = atTbilisi(2026, 8, 4, 17, 0);

        assertThat(invokeGetCacheWindowTtl(null, now)).isEqualTo(900L);
    }

    @Test
    void cacheWindowMeta_afterWindowWithTodayRssDate_shouldHoldUntilNextWindow() throws Exception {
        // 2026-08-04 20:00 Tbilisi, cached RSS date is today -> hold until 2026-08-05 16:00
        long now = atTbilisi(2026, 8, 4, 20, 0);
        long expected = 20 * 3600L; // 16:00 tomorrow minus 20:00 today

        assertThat(invokeGetCacheWindowTtl("2026-08-04", now)).isEqualTo(expected);
    }

    @Test
    void cacheWindowMeta_beforeWindow_shouldUseTtlUntilWindowStart() throws Exception {
        // 2026-08-04 10:00 Tbilisi -> 6 hours until 16:00
        long now = atTbilisi(2026, 8, 4, 10, 0);

        assertThat(invokeGetCacheWindowTtl(null, now)).isEqualTo(6 * 3600L);
    }

    @Test
    void cacheWindowMeta_afterWindowWithStaleRssDate_shouldUseFallbackTtl() throws Exception {
        // 2026-08-04 20:00 Tbilisi, cached RSS date is from a previous day
        long now = atTbilisi(2026, 8, 4, 20, 0);

        assertThat(invokeGetCacheWindowTtl("2026-08-03", now)).isEqualTo(3600L);
    }

    // ─────────────────────────────────────────────────────────────────────────────
    //  Graceful degradation
    // ─────────────────────────────────────────────────────────────────────────────

    @Test
    void getRates_whenSourceUnreachableAndNoCache_shouldReturnEmptyRates() {
        NbgCurrencyServiceImpl offlineService =
                new NbgCurrencyServiceImpl("https://127.0.0.1:1/nonexistent");

        CurrencyRatesResponse response = offlineService.getRates();

        assertThat(response.getRates()).isEmpty();
        assertThat(response.getRss_date()).isNull();
    }

    // ─────────────────────────────────────────────────────────────────────────────
    //  Helpers
    // ─────────────────────────────────────────────────────────────────────────────

    private long atTbilisi(int year, int month, int day, int hour, int minute) {
        return ZonedDateTime.of(year, month, day, hour, minute, 0, 0, TBILISI).toEpochSecond();
    }

    private Document parseXml(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new InputSource(new StringReader(xml)));
    }

    private CurrencyRatesResponse invokeParseRates(Document doc) throws Exception {
        Method method = NbgCurrencyServiceImpl.class.getDeclaredMethod("parseRates", Document.class);
        method.setAccessible(true);
        return (CurrencyRatesResponse) method.invoke(service, doc);
    }

    private long invokeGetCacheWindowTtl(String rssDate, long nowSeconds) throws Exception {
        Method method = NbgCurrencyServiceImpl.class.getDeclaredMethod(
                "getCacheWindowMeta", String.class, long.class);
        method.setAccessible(true);
        Object meta = method.invoke(service, rssDate, nowSeconds);
        Method ttlAccessor = meta.getClass().getDeclaredMethod("ttl");
        ttlAccessor.setAccessible(true);
        return (long) ttlAccessor.invoke(meta);
    }
}
