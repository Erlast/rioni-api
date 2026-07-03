package com.rioni.lk.api.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Component
@Order(1)
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String requestId = UUID.randomUUID().toString().substring(0, 8);
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();
        String ip = getClientIp(request);
        String timestamp = LocalDateTime.now().format(FORMATTER);

        String fullPath = (queryString != null) ? uri + "?" + queryString : uri;

        log.info("[{}] → {} {} | IP: {} | {}", requestId, method, fullPath, ip, timestamp);

        long startTimeMs = System.currentTimeMillis();

        try {
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTimeMs;
            log.error("[{}] ✗ {} {} | IP: {} | Status: ERROR | Duration: {}ms | Exception: {}: {} at {}",
                    requestId, method, fullPath, ip, duration,
                    e.getClass().getName(), e.getMessage(), getExceptionLocation(e));
            throw e;
        }

        long duration = System.currentTimeMillis() - startTimeMs;
        int status = response.getStatus();

        if (status >= 500) {
            log.error("[{}] ✗ {} {} | IP: {} | Status: {} | Duration: {}ms",
                    requestId, method, fullPath, ip, status, duration);
        } else if (status >= 400) {
            log.warn("[{}] ✗ {} {} | IP: {} | Status: {} | Duration: {}ms",
                    requestId, method, fullPath, ip, status, duration);
        } else {
            log.info("[{}] ← {} {} | IP: {} | Status: {} | Duration: {}ms",
                    requestId, method, fullPath, ip, status, duration);
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return (ip != null) ? ip : "unknown";
    }

    private String getExceptionLocation(Exception e) {
        StackTraceElement[] stackTrace = e.getStackTrace();
        if (stackTrace != null && stackTrace.length > 0) {
            StackTraceElement top = stackTrace[0];
            return top.getClassName() + "." + top.getMethodName() + "("
                    + top.getFileName() + ":" + top.getLineNumber() + ")";
        }
        return "unknown";
    }
}
