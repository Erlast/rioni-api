package com.rioni.lk.api.controller;

import com.rioni.lk.api.AbstractIntegrationTest;
import com.rioni.lk.api.dto.TariffSelectionRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

class ProfileControllerTest extends AbstractIntegrationTest {

    private static final int TEST_PROFILE_ID = 200;
    private static final String TEST_LOGIN = "tariffuser";

    // Tariffs are seeded in test-data.sql (ids 1, 2, 3)
    private static final int EXISTING_TARIFF_ID = 2;
    private static final int NON_EXISTENT_TARIFF_ID = 99999;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        // Clean up any existing test data from previous runs
        jdbcTemplate.update("DELETE FROM profile WHERE id = ?", TEST_PROFILE_ID);

        // Create fresh test profile
        jdbcTemplate.update("INSERT INTO profile (id, login) VALUES (?, ?)",
                TEST_PROFILE_ID, TEST_LOGIN);
    }

    @Test
    void saveTariff_withExistingTariff_shouldReturn200AndSaveTariffId() {
        // Arrange
        String token = createAccessToken(TEST_PROFILE_ID);
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<TariffSelectionRequest> request = new HttpEntity<>(
                new TariffSelectionRequest(EXISTING_TARIFF_ID), headers);

        // Act
        ResponseEntity<Void> response = restTemplate.exchange(
                baseUrl() + "/api/profile/tariff",
                HttpMethod.POST,
                request,
                Void.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        Integer savedTariffId = jdbcTemplate.queryForObject(
                "SELECT tariff_id FROM profile WHERE id = ?",
                Integer.class,
                TEST_PROFILE_ID);
        assertThat(savedTariffId).isEqualTo(EXISTING_TARIFF_ID);
    }

    @Test
    void saveTariff_withNonExistentTariff_shouldReturn400AndMessage() {
        // Arrange
        String token = createAccessToken(TEST_PROFILE_ID);
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<TariffSelectionRequest> request = new HttpEntity<>(
                new TariffSelectionRequest(NON_EXISTENT_TARIFF_ID), headers);

        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl() + "/api/profile/tariff",
                HttpMethod.POST,
                request,
                String.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).contains("Tariff not found");

        // The tariff must NOT be saved on the profile
        Integer savedTariffId = jdbcTemplate.queryForObject(
                "SELECT tariff_id FROM profile WHERE id = ?",
                Integer.class,
                TEST_PROFILE_ID);
        assertThat(savedTariffId).isNull();
    }

    @Test
    void saveTariff_withoutToken_shouldReturn403() {
        // Arrange
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<TariffSelectionRequest> request = new HttpEntity<>(
                new TariffSelectionRequest(EXISTING_TARIFF_ID), headers);

        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl() + "/api/profile/tariff",
                HttpMethod.POST,
                request,
                String.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
}
