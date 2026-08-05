package com.rioni.lk.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CurrencyRatesResponse {
    private List<CurrencyRateDto> rates;
    private String rss_date;
}
