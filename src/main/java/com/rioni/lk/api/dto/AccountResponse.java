package com.rioni.lk.api.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class AccountResponse {
    private BigDecimal totalSum;
    private List<AccountDto> accounts;
}
