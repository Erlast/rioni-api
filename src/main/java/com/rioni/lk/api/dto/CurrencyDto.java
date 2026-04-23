package com.rioni.lk.api.dto;

import com.rioni.lk.api.model.Currency;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CurrencyDto {
    private int id;
    private String title;
    private String symbol;

    public CurrencyDto(Currency currency) {
        this.id = currency.getId();
        this.title = currency.getTitle();
        this.symbol = currency.getSymbol();
    }
}
