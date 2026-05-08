package com.rioni.lk.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "quote")
public class Quote {
    @Id
    @Column(name = "asset_id")
    private int assetId;

    @Column(name = "date")
    private String date;

     @Column(name = "market_id")
    private int marketId;

     @Column(name = "quote_type_code")
    private String quoteTypeCode;

     @Column(name = "price_currency_id")
    private int priceCurrencyId;

     @Column(name = "quote_value")
    private int quoteValue;

    @Column(name = "accrued_interest")
    private int accruedInterest;
}