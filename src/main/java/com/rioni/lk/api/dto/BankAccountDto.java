package com.rioni.lk.api.dto;

import lombok.Data;

@Data
public class BankAccountDto {
    private int id;
    private String country;
    private String bankName;
    private String iban;
    private String swift;
    private Boolean isMain;
    private Boolean isConfirmed;
    private Boolean isBlocked;
}