package com.rioni.lk.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "subaccount_asset")
@IdClass(SubaccountAssetId.class)
public class SubaccountAsset {
    @Id
    @Column(name = "subaccount_id")
    private int subaccountId;

    @Column(name = "date")
    private String date;

    @Id
    @Column(name = "asset_id")
    private int assetId;

    @Column(name = "amount")
    private int amount;

    @Column(name = "purchase_price")
    private int purchasePrice;

    @Column(name = "currency_id")
    private int currencyId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", insertable = false, updatable = false)
    private Asset asset;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subaccount_id", insertable = false, updatable = false)
    private Subaccount subaccount;
}