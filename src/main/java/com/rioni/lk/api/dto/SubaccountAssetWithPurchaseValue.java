package com.rioni.lk.api.dto;

import java.math.BigDecimal;
import com.rioni.lk.api.model.SubaccountAsset;

public record SubaccountAssetWithPurchaseValue(SubaccountAsset subaccountAsset,
    BigDecimal purchaseValue) {
    
}
