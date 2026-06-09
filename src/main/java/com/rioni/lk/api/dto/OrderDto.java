package com.rioni.lk.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDto {
    private long id;
    private Long accountId;
    private String orderNumber;
    private LocalDateTime createdAt;
    private LocalDateTime executedAt;
    private String operation;
    private String cb;
    private BigDecimal quantity;
    private BigDecimal amount;
    private int status;
}
