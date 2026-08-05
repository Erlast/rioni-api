package com.rioni.lk.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CurrencyRateDto {
    private String code;
    private String rate;
    private String change;
    private String direction;
    private String icon;
}
