package com.rioni.lk.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${app.avatars.storage-path}")
    private String storagePath;

    @Value("${app.avatars.base-url}")
    private String baseUrl;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/profile/avatars/**")
            .addResourceLocations("file:" + storagePath + "/");
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getStoragePath() {
        return storagePath;
    }


}