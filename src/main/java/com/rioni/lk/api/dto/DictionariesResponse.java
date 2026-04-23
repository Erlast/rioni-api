package com.rioni.lk.api.dto;

import com.rioni.lk.api.dto.CurrencyDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DictionariesResponse {
    private Map<String, List<?>> dictionaries;

    public DictionariesResponse(List<CurrencyDto> currencies) {
        this.dictionaries = Map.of("currencies", currencies);
    }
}
