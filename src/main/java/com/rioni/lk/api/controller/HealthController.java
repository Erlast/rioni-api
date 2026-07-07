package com.rioni.lk.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/api/health")
    public ResponseEntity<Map<String, Object>> health() {
        return new ResponseEntity<>(
            Map.of(
                "status", "UP",
                "timestamp", Instant.now().toString()
            ),
            HttpStatus.OK
        );
    }
}
