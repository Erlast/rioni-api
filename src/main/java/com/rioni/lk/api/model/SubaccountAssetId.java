package com.rioni.lk.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class SubaccountAssetId implements Serializable {
    @Column(name = "subaccount_id")
    private int subaccountId;

    @Column(name = "asset_id")
    private int assetId;
}
