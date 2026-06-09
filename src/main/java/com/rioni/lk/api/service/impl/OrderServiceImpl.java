package com.rioni.lk.api.service.impl;

import com.rioni.lk.api.dto.OrderDto;
import com.rioni.lk.api.dto.OrdersResponse;
import com.rioni.lk.api.model.Order;
import com.rioni.lk.api.repository.OrderRepository;
import com.rioni.lk.api.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    @Autowired
    public OrderServiceImpl(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public OrdersResponse getOrdersByAccountId(Long accountId, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Order> orderPage = orderRepository.findByAccountId(accountId, pageRequest);
        List<OrderDto> orderDtos = orderPage.getContent().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return new OrdersResponse(orderDtos, orderPage.getTotalElements());
    }

    private OrderDto toDto(Order order) {
        return new OrderDto(
                order.getId(),
                order.getAccountId(),
                order.getOrderNumber(),
                order.getCreatedAt(),
                order.getExecutedAt(),
                order.getOperation(),
                order.getCb(),
                order.getQuantity(),
                order.getAmount(),
                order.getStatus()
        );
    }
}
