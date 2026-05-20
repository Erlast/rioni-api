package com.rioni.lk.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountYieldDto {
    @JsonProperty("accountYield")
    private BigDecimal accountYield;

    @JsonProperty("accountPercent")
    private BigDecimal accountPercent;
}
