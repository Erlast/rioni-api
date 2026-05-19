package com.rioni.lk.api.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class LogosConfig {

    @Value("${app.logos.base-url:}")
    private String logosBaseUrl;

    public static String LOGOS_BASE_URL;

    @PostConstruct
    public void init() {
        LOGOS_BASE_URL = logosBaseUrl;
    }
}
