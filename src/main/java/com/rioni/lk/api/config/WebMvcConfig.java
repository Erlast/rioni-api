package com.rioni.lk.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${app.avatars.storage-path}")
    private String storagePath;

    @Value("${app.avatars.base-url}")
    private String baseUrl;

    @Value("${app.uploads.storage-path}")
    private String uploadsStoragePath;

    @Value("${app.uploads.base-url}")
    private String uploadsBaseUrl;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/profile/avatars/**")
            .addResourceLocations("file:" + Paths.get(storagePath).toAbsolutePath().normalize().toString() + "/");
        registry.addResourceHandler("/images/**")
            .addResourceLocations("classpath:/static/");
        registry.addResourceHandler("/uploads/**")
            .addResourceLocations("file:" + Paths.get(uploadsStoragePath).toAbsolutePath().normalize().toString() + "/");
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getStoragePath() {
        return storagePath;
    }

    public String getUploadsStoragePath() {
        return uploadsStoragePath;
    }

    public String getUploadsBaseUrl() {
        return uploadsBaseUrl;
    }
}