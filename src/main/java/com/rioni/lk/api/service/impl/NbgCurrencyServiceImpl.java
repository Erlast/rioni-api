package com.rioni.lk.api.service.impl;

import com.rioni.lk.api.dto.CurrencyRateDto;
import com.rioni.lk.api.dto.CurrencyRatesResponse;
import com.rioni.lk.api.service.NbgCurrencyService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Java port of the PHP script {@code src/main/resources/currencies/nbg-currency-widget-full.php}.
 * Fetches currency rates from the National Bank of Georgia RSS feed, parses them,
 * caches the result in memory and returns them sorted with priority currencies first.
 */
@Service
public class NbgCurrencyServiceImpl implements NbgCurrencyService {

    private static final ZoneId TBILISI = ZoneId.of("Asia/Tbilisi");

    private static final Pattern DATE_PATTERN = Pattern.compile("(\\d{4}-\\d{2}-\\d{2})");

    private static final List<String> PRIORITY_CURRENCIES =
            List.of("USD", "EUR", "GBP", "RUB", "AMD", "AZN", "TRY");

    /** Long-term fallback cache TTL (2 days) */
    private static final long FALLBACK_TTL_SECONDS = 172_800L;
    /** TTL used while inside the 16:00-19:00 Tbilisi update window */
    private static final long UPDATE_WINDOW_TTL_SECONDS = 900L;
    /** Minimal TTL for the remaining cache modes */
    private static final long MIN_TTL_SECONDS = 900L;
    /** Default/fallback TTL */
    private static final long DEFAULT_TTL_SECONDS = 3600L;

    private static final String CACHE_ID = "nbg_currency_widget_full_embed_v3";
    private static final String USER_AGENT = "RioniCapitalCurrencyWidgetFull/1.0";

    private final String rssUrl;
    private final HttpClient httpClient;

    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    public NbgCurrencyServiceImpl(
            @Value("${app.nbg.currency-rss-url:https://nbg.gov.ge/gw/api/ct/monetarypolicy/currencies/en/rss}")
            String rssUrl) {
        this.rssUrl = rssUrl;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    @Override
    public CurrencyRatesResponse getRates() {
        long now = ZonedDateTime.now(TBILISI).toEpochSecond();

        CacheEntry cached = cache.get(CACHE_ID);

        // Last known good data cached within the last 2 days
        CurrencyRatesResponse fallbackData = null;
        if (cached != null && now - cached.cachedAtSeconds() < FALLBACK_TTL_SECONDS) {
            fallbackData = cached.data();
        }

        String cachedRssDate = fallbackData != null ? fallbackData.getRss_date() : null;
        long ttl = getCacheWindowMeta(cachedRssDate, now).ttl();

        // Short-term cache window is still valid -> serve from cache
        if (cached != null && now - cached.cachedAtSeconds() < ttl
                && cached.data() != null && !cached.data().getRates().isEmpty()) {
            return cached.data();
        }

        // Try to fetch fresh data from NBG
        CurrencyRatesResponse freshData = fetchAndParse();

        if (freshData != null && !freshData.getRates().isEmpty()) {
            cache.put(CACHE_ID, new CacheEntry(freshData, now));
            return freshData;
        }

        // NBG is unreachable -> fall back to stale cached data
        if (fallbackData != null && !fallbackData.getRates().isEmpty()) {
            return fallbackData;
        }

        return new CurrencyRatesResponse(List.of(), null);
    }

    /**
     * Mirrors rioniGetCacheWindowMetaFull(): computes the cache TTL depending on
     * the current time in Tbilisi and the date of the cached RSS data.
     */
    private CacheWindowMeta getCacheWindowMeta(String rssDate, long nowSeconds) {
        ZonedDateTime now = ZonedDateTime.ofInstant(Instant.ofEpochSecond(nowSeconds), TBILISI);
        LocalDate today = now.toLocalDate();
        String todayStr = today.toString();

        long windowStartTs = today.atTime(16, 0).atZone(TBILISI).toEpochSecond();
        long windowEndTs = today.atTime(19, 0).atZone(TBILISI).toEpochSecond();

        if (nowSeconds >= windowStartTs && nowSeconds < windowEndTs) {
            return new CacheWindowMeta("update_window", UPDATE_WINDOW_TTL_SECONDS);
        }

        if (nowSeconds >= windowEndTs && todayStr.equals(rssDate)) {
            long nextStartTs = today.plusDays(1).atTime(16, 0).atZone(TBILISI).toEpochSecond();
            long ttl = Math.max(MIN_TTL_SECONDS, nextStartTs - nowSeconds);
            return new CacheWindowMeta("night_hold", ttl);
        }

        if (nowSeconds < windowStartTs) {
            long ttl = Math.max(MIN_TTL_SECONDS, windowStartTs - nowSeconds);
            return new CacheWindowMeta("before_update_window", ttl);
        }

        return new CacheWindowMeta("fallback", DEFAULT_TTL_SECONDS);
    }

    /**
     * Mirrors rioniLoadXmlFull() + rioniParseRatesFromXmlFull().
     */
    private CurrencyRatesResponse fetchAndParse() {
        String xmlString = loadRssString(rssUrl);
        if (xmlString == null) {
            return null;
        }

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(false);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);

            DocumentBuilder builder = factory.newDocumentBuilder();
            org.w3c.dom.Document xml = builder.parse(new InputSource(new StringReader(xmlString)));

            return parseRates(xml);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Mirrors rioniLoadXmlStringFull(): fetches the RSS with a 10s connect / 20s
     * request timeout and the same User-Agent.
     */
    private String loadRssString(String url) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(20))
                    .header("User-Agent", USER_AGENT)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200 || response.body().isBlank()) {
                return null;
            }
            return response.body();
        } catch (Exception e) {
            return null;
        }
    }

    private CurrencyRatesResponse parseRates(org.w3c.dom.Document xml) {
        String rssDate = extractRatesDate(xml);
        String descriptionHtml = extractDescriptionHtml(xml);

        if (descriptionHtml == null) {
            return new CurrencyRatesResponse(List.of(), rssDate);
        }

        Map<String, CurrencyRateDto> ratesByCode = parseRatesHtml(descriptionHtml);

        return new CurrencyRatesResponse(sortRates(ratesByCode), rssDate);
    }

    /**
     * Mirrors rioniExtractRatesDateFull(): takes the first item's title
     * and extracts the first YYYY-MM-DD date from it.
     */
    private String extractRatesDate(org.w3c.dom.Document xml) {
        NodeList items = xml.getElementsByTagName("item");
        if (items.getLength() == 0) {
            return null;
        }

        org.w3c.dom.Element firstItem = (org.w3c.dom.Element) items.item(0);
        NodeList titles = firstItem.getElementsByTagName("title");
        if (titles.getLength() == 0) {
            return null;
        }

        String title = titles.item(0).getTextContent();
        Matcher matcher = DATE_PATTERN.matcher(title);
        return matcher.find() ? matcher.group(1) : null;
    }

    private String extractDescriptionHtml(org.w3c.dom.Document xml) {
        NodeList items = xml.getElementsByTagName("item");
        if (items.getLength() == 0) {
            return null;
        }

        org.w3c.dom.Element firstItem = (org.w3c.dom.Element) items.item(0);
        NodeList descriptions = firstItem.getElementsByTagName("description");
        if (descriptions.getLength() == 0) {
            return null;
        }

        String html = descriptions.item(0).getTextContent();
        return html.isBlank() ? null : html;
    }

    /**
     * Mirrors the DOM parsing inside rioniParseRatesFromXmlFull(): iterates over
     * table rows with at least 5 cells, reads code/rate/change and the direction
     * from the green/red.gif icon image.
     */
    private Map<String, CurrencyRateDto> parseRatesHtml(String descriptionHtml) {
        Map<String, CurrencyRateDto> result = new LinkedHashMap<>();

        Document html = Jsoup.parse(descriptionHtml);

        for (Element row : html.select("tr")) {
            Elements tds = row.select("td");
            if (tds.size() < 5) {
                continue;
            }

            String code = tds.get(0).text().trim();
            String rate = tds.get(2).text().trim();
            String change = tds.get(4).text().trim();

            if (code.isEmpty()) {
                continue;
            }

            String direction = "neutral";
            String directionIcon = "•";

            Element img = tds.get(3).selectFirst("img");
            if (img != null) {
                String src = img.attr("src");
                if (src.contains("green.gif")) {
                    direction = "up";
                    directionIcon = "▲";
                } else if (src.contains("red.gif")) {
                    direction = "down";
                    directionIcon = "▼";
                }
            }

            result.put(code, new CurrencyRateDto(code, rate, change, direction, directionIcon));
        }

        return result;
    }

    /**
     * Mirrors rioniSortRatesFull(): priority currencies first in the predefined
     * order, then the rest sorted alphabetically by code.
     */
    private List<CurrencyRateDto> sortRates(Map<String, CurrencyRateDto> ratesByCode) {
        Map<String, CurrencyRateDto> orderedPriority = new LinkedHashMap<>();
        List<CurrencyRateDto> otherPart = new ArrayList<>();

        for (String code : PRIORITY_CURRENCIES) {
            CurrencyRateDto item = ratesByCode.get(code);
            if (item != null) {
                orderedPriority.put(code, item);
            }
        }

        for (Map.Entry<String, CurrencyRateDto> entry : ratesByCode.entrySet()) {
            if (!orderedPriority.containsKey(entry.getKey())) {
                otherPart.add(entry.getValue());
            }
        }

        otherPart.sort(Comparator.comparing(CurrencyRateDto::getCode));

        List<CurrencyRateDto> sorted = new ArrayList<>(orderedPriority.values());
        sorted.addAll(otherPart);
        return sorted;
    }

    private record CacheEntry(CurrencyRatesResponse data, long cachedAtSeconds) {}

    private record CacheWindowMeta(String mode, long ttl) {}
}
