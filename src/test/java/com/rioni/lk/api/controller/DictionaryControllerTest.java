package com.rioni.lk.api.controller;

import com.rioni.lk.api.AbstractIntegrationTest;
import com.rioni.lk.api.dto.DictionariesResponse;
import com.rioni.lk.api.dto.GlossaryEntryDto;
import com.rioni.lk.api.dto.TariffDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DictionaryControllerTest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void getDictionaries_withValidToken_shouldReturn200AndCurrencies() {
        // Arrange
        String token = createAccessToken(1);
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        // Act
        ResponseEntity<DictionariesResponse> response = restTemplate.exchange(
                baseUrl() + "/api/dictionaries/",
                HttpMethod.GET,
                request,
                DictionariesResponse.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        Map<String, List<?>> dictionaries = response.getBody().getDictionaries();
        assertThat(dictionaries).containsKey("currencies");

        List<?> currencies = dictionaries.get("currencies");
        assertThat(currencies).isNotEmpty();

        // Verify each currency has the expected structure (id, title, symbol)
        for (Object currency : currencies) {
            assertThat(currency).isInstanceOf(Map.class);
            @SuppressWarnings("unchecked")
            Map<String, Object> currencyMap = (Map<String, Object>) currency;
            assertThat(currencyMap).containsKeys("id", "title", "symbol");
        }
    }

    @Test
    void getDictionaries_withoutToken_shouldReturn403() {
        // Arrange
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Void> request = new HttpEntity<>(headers);

        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl() + "/api/dictionaries/",
                HttpMethod.GET,
                request,
                String.class
        );

        // Assert
        // Spring Security returns 403 Forbidden for unauthenticated requests
        // to protected endpoints (not 401, since no credentials were provided)
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void getDictionaries_withInvalidToken_shouldReturn403() {
        // Arrange
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("invalid-token");
        HttpEntity<Void> request = new HttpEntity<>(headers);

        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl() + "/api/dictionaries/",
                HttpMethod.GET,
                request,
                String.class
        );

        // Assert
        // Spring Security returns 403 Forbidden when JWT token is malformed/invalid
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    // ─────────────────────────────────────────────────────────────────────────────
    //  /dictionaries/glossary/
    // ─────────────────────────────────────────────────────────────────────────────

    @Test
    void getGlossary_withValidTokenAndRussianLanguage_shouldReturn200AndRussianEntries() {
        // Arrange
        String token = createAccessToken(1);
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        // Act
        ResponseEntity<GlossaryEntryDto[]> response = restTemplate.exchange(
                baseUrl() + "/api/dictionaries/glossary/?lang=ru",
                HttpMethod.GET,
                request,
                GlossaryEntryDto[].class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isNotEmpty();

        for (GlossaryEntryDto entry : response.getBody()) {
            assertThat(entry.getLanguage()).isEqualTo("ru");
            assertThat(entry.getTerm()).isNotBlank();
            assertThat(entry.getDefinition()).isNotBlank();
        }
    }

    @Test
    void getGlossary_withValidTokenAndEnglishLanguage_shouldReturn200AndEnglishEntries() {
        // Arrange
        String token = createAccessToken(1);
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        // Act
        ResponseEntity<GlossaryEntryDto[]> response = restTemplate.exchange(
                baseUrl() + "/api/dictionaries/glossary/?lang=en",
                HttpMethod.GET,
                request,
                GlossaryEntryDto[].class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isNotEmpty();

        for (GlossaryEntryDto entry : response.getBody()) {
            assertThat(entry.getLanguage()).isEqualTo("en");
        }
    }

    @Test
    void getGlossary_withLetterFilter_shouldReturnOnlyEntriesForThatLetter() {
        // Arrange
        String token = createAccessToken(1);
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        // Build the URI with UriComponentsBuilder so the Cyrillic letter
        // is properly percent-encoded (a pre-encoded string passed to
        // RestTemplate would be re-encoded and double-encoded).
        URI uri = UriComponentsBuilder
                .fromHttpUrl(baseUrl() + "/api/dictionaries/glossary/")
                .queryParam("lang", "ru")
                .queryParam("letter", "\u0410") // Cyrillic 'А' (U+0410)
                .build()
                .encode()
                .toUri();

        // Act
        ResponseEntity<GlossaryEntryDto[]> response = restTemplate.exchange(
                uri,
                HttpMethod.GET,
                request,
                GlossaryEntryDto[].class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isNotEmpty();

        for (GlossaryEntryDto entry : response.getBody()) {
            assertThat(entry.getLanguage()).isEqualTo("ru");
            assertThat(entry.getLetter()).isEqualTo("\u0410");
        }
    }

    @Test
    void getGlossary_withLanguageHavingNoEntries_shouldReturn200AndEmptyList() {
        // Arrange
        String token = createAccessToken(1);
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        // Act
        ResponseEntity<GlossaryEntryDto[]> response = restTemplate.exchange(
                baseUrl() + "/api/dictionaries/glossary/?lang=fr",
                HttpMethod.GET,
                request,
                GlossaryEntryDto[].class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    void getGlossary_withoutLanguageParam_shouldReturn400() {
        // Arrange
        String token = createAccessToken(1);
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl() + "/api/dictionaries/glossary/",
                HttpMethod.GET,
                request,
                String.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void getGlossary_withoutToken_shouldReturn403() {
        // Arrange
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Void> request = new HttpEntity<>(headers);

        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl() + "/api/dictionaries/glossary/?lang=ru",
                HttpMethod.GET,
                request,
                String.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    // ─────────────────────────────────────────────────────────────────────────────
    //  GET /dictionaries/tariffs
    // ─────────────────────────────────────────────────────────────────────────────

    @Test
    void getTariffs_withValidToken_shouldReturn200AndListOfTariffs() {
        // Arrange
        String token = createAccessToken(1);
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        // Act
        ResponseEntity<TariffDto[]> response = restTemplate.exchange(
                baseUrl() + "/api/dictionaries/tariffs",
                HttpMethod.GET,
                request,
                TariffDto[].class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isNotEmpty();

        // Verify each tariff has the expected structure (id, name, description)
        for (TariffDto tariff : response.getBody()) {
            assertThat(tariff.getId()).isPositive();
            assertThat(tariff.getName()).isNotBlank();
            assertThat(tariff.getDescription()).isNotNull();
        }
    }

    @Test
    void getTariffs_withoutToken_shouldReturn403() {
        // Arrange
        HttpEntity<Void> request = new HttpEntity<>(new HttpHeaders());

        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl() + "/api/dictionaries/tariffs",
                HttpMethod.GET,
                request,
                String.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
}
