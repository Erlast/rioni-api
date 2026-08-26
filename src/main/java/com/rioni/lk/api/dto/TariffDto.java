package com.rioni.lk.api.dto;

import com.rioni.lk.api.model.Tariff;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TariffDto {
    private int id;
    private String name;
    private String description;

    public TariffDto(Tariff tariff) {
        this.id = tariff.getId();
        this.name = tariff.getName();
        this.description = tariff.getDescription();
    }
}
