package com.rioni.lk.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.rioni.lk.api.model.Account;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties({"profileId"})
public class AccountDto {
    private int id;
    private int profileId;
    private String accountNumber;
    private String accountType;
    private int accountCurrencyId;
    private BigDecimal balance;
    private BigDecimal deposit;
    private BigDecimal fundsInTransit;
    private BigDecimal tradingFunds;

    public AccountDto(Account account) {
        this.id = account.getId();
        this.profileId = account.getProfileId();
        this.accountNumber = account.getAccountNumber();
        this.accountType = account.getAccountType();
        this.accountCurrencyId = account.getAccountCurrencyId();
    }
}