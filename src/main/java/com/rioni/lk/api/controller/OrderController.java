package com.rioni.lk.api.controller;

import com.rioni.lk.api.dto.OrdersResponse;
import com.rioni.lk.api.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class OrderController {

    private static final int DEFAULT_PAGE_SIZE = 10;

    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    private Long getCurrentProfileId() {
        Integer profileId = (Integer) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return profileId.longValue();
    }

    @GetMapping("/orders/{accountId}")
    public ResponseEntity<OrdersResponse> getOrders(
            @PathVariable Long accountId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String operationType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime periodStart,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime periodEnd,
            @RequestParam(required = false) Integer status) {
        Long profileId = getCurrentProfileId();
        OrdersResponse orders = orderService.getOrdersByAccountId(
                accountId, page, limit, operationType, periodStart, periodEnd, status);
        
        HttpHeaders headers = new HttpHeaders();
        headers.add("x-orders-total", String.valueOf(orders.getTotal()));
        
        return new ResponseEntity<>(orders, headers, HttpStatus.OK);
    }
}
