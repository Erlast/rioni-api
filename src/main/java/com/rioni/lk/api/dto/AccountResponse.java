package com.rioni.lk.api.dto;

import lombok.Data;
import java.util.List;

@Data
public class AccountResponse {
    private List<AccountDto> accounts;
}
