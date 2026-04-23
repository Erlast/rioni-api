package com.rioni.lk.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.rioni.lk.api.model.Account;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties({"id", "profileId"})
public class AccountDto {
    private int id;
    private int profileId;
    private String accountNumber;
    private String accountType;
    private int accountCurrencyId;

    public AccountDto(Account account) {
        this.id = account.getId();
        this.profileId = account.getProfileId();
        this.accountNumber = account.getAccountNumber();
        this.accountType = account.getAccountType();
        this.accountCurrencyId = account.getAccountCurrencyId();
    }
}
