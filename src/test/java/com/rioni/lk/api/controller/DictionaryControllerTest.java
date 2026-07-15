package com.rioni.lk.api.controller;

import com.rioni.lk.api.AbstractIntegrationTest;
import com.rioni.lk.api.dto.DictionariesResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

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
}
