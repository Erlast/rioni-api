package com.rioni.lk.api.dto;

import com.querydsl.core.Tuple;
import com.rioni.lk.api.model.SubaccountAsset;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubaccountAssetDto {
    private int subaccountId;
    private int assetId;
    private int purchasePrice;
    private int amount;
    private int currencyId;
    private String assetName;
    private String baseTicker;
    private BigDecimal investedValue;
    private int balanceValue;
    private int bid;
    private int ask;
    private int profit;
    private String logo;
    private boolean active;

    /**
     * Build DTO from a Querydsl Tuple returned by {@code SubaccountAssetRepositoryCustom}.
     * <p>Tuple layout:
     *   [0] SubaccountAsset  (entity)
     *   [1] BigDecimal       investedValue
     *   [2] Integer          balanceValue  (last quote)
     *   [3] Integer          bid
     *   [4] Integer          ask
     */
    public SubaccountAssetDto(Tuple tuple) {
        SubaccountAsset saa = tuple.get(0, SubaccountAsset.class);
        this.subaccountId = saa.getSubaccountId();
        this.assetId = saa.getAssetId();
        this.amount = saa.getAmount();
        this.purchasePrice = saa.getPurchasePrice();
        this.currencyId = saa.getCurrencyId();
        this.assetName = saa.getAsset().getAssetName();
        this.baseTicker = saa.getAsset().getBaseTicker();
        this.investedValue = tuple.get(1, BigDecimal.class);
        this.balanceValue = valueOrZero(tuple.get(2, Integer.class));
        this.bid = valueOrZero(tuple.get(3, Integer.class));
        this.ask = valueOrZero(tuple.get(4, Integer.class));
        this.profit = this.balanceValue - this.investedValue.intValue();
        this.logo = saa.getAsset().getAssetId() + ".png";
        this.active = true;
    }

    private static int valueOrZero(Integer value) {
        return value != null ? value : 0;
    }
}
