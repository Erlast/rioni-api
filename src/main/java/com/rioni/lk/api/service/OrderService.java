package com.rioni.lk.api.service;

import com.rioni.lk.api.dto.OrdersResponse;

import java.time.LocalDateTime;

public interface OrderService {
    OrdersResponse getOrdersByAccountId(Long accountId, int page, int limit,
                                        String operationType,
                                        LocalDateTime periodStart,
                                        LocalDateTime periodEnd,
                                        Integer status);
}
