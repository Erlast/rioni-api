package com.rioni.lk.api.service;

import com.rioni.lk.api.dto.OrdersResponse;

public interface OrderService {
    OrdersResponse getOrdersByAccountId(Long accountId, int page, int size);
}
