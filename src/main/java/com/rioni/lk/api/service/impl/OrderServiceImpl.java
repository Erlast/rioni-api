package com.rioni.lk.api.service.impl;

import com.rioni.lk.api.dto.OrderDto;
import com.rioni.lk.api.dto.OrdersResponse;
import com.rioni.lk.api.model.Order;
import com.rioni.lk.api.repository.OrderRepository;
import com.rioni.lk.api.service.OrderService;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
    public OrdersResponse getOrdersByAccountId(Long accountId, int page, int limit,
                                                String operationType,
                                                LocalDateTime periodStart,
                                                LocalDateTime periodEnd,
                                                Integer status) {
        // Normalize operationType: '+' in query strings may not be decoded to space
        final String normalizedOperationType = (operationType != null)
                ? operationType.replace('+', ' ').trim()
                : null;

        Specification<Order> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(criteriaBuilder.equal(root.get("accountId"), accountId));

            if (normalizedOperationType != null && !normalizedOperationType.isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("operation"), normalizedOperationType));
            }

            if (periodStart != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("executedAt"), periodStart));
            }

            if (periodEnd != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("executedAt"), periodEnd));
            }

            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        PageRequest pageRequest = PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Order> orderPage = orderRepository.findAll(spec, pageRequest);
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
