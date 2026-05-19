package com.rioni.lk.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.rioni.lk.api.model.SubaccountAsset;
import com.rioni.lk.api.model.Asset;
import com.rioni.lk.api.config.LogosConfig;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.core.io.ClassPathResource;

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

    public SubaccountAssetDto(Object[] row) {
        SubaccountAsset saa = (SubaccountAsset) row[0];
        this.subaccountId= saa.getSubaccountId();
        this.assetId = saa.getAssetId();
        this.amount = saa.getAmount();
        this.purchasePrice = saa.getPurchasePrice();
        this.currencyId = saa.getCurrencyId();
        this.assetName = saa.getAsset().getAssetName();
        this.baseTicker = saa.getAsset().getBaseTicker();
        this.investedValue = (BigDecimal) row[1];
        this.balanceValue = row[2] != null ? (Integer) row[2] : 0;
        this.bid = row[3] != null ? (Integer) row[3] : 0;
        this.ask = row[4] != null ? (Integer) row[4] : 0;
        this.profit = this.balanceValue - this.investedValue.intValue();
        String logoPath = "/images/logos/" + saa.getAsset().getAssetId() + ".png";
        try {
            new ClassPathResource("static" + logoPath).getInputStream().close();
            this.logo = LogosConfig.LOGOS_BASE_URL + logoPath;
        } catch (Exception e) {
            this.logo = null;
        }

        this.active = true;

    }
}
