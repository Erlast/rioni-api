package com.rioni.lk.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "asset")
public class Asset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "asset_id")
    private int assetId;

    @Column(name = "asset_name")
    private String assetName;

     @Column(name = "base_ticker")
    private String baseTicker;

     @Column(name = "base_market_id", nullable = true)
    private Integer baseMarketId;

     @Column(name = "asset_type_code")
    private String assetTypeCode;

     @Column(name = "isin")
    private String isin;
}