package com.rioni.lk.api.controller;

import com.rioni.lk.api.dto.OrdersResponse;
import com.rioni.lk.api.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
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
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long profileId = getCurrentProfileId();
        OrdersResponse orders = orderService.getOrdersByAccountId(accountId, page, size);
        
        HttpHeaders headers = new HttpHeaders();
        headers.add("x-orders-total", String.valueOf(orders.getTotal()));
        
        return new ResponseEntity<>(orders, headers, HttpStatus.OK);
    }
}
