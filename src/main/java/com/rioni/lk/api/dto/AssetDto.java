package com.rioni.lk.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.rioni.lk.api.model.Asset;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AssetDto {
    private int assetId;
    private String assetName;
    private String baseTicker;
    private int baseMarketId;
    private String assetTypeCode;
    private String isin;

    public AssetDto(Asset asset) {
        this.assetId = asset.getAssetId();
        this.assetName = asset.getAssetName();
        this.baseTicker = asset.getBaseTicker();
        this.baseMarketId = asset.getBaseMarketId();
        this.assetTypeCode = asset.getAssetTypeCode();
        this.isin = asset.getIsin();
    }
}
