package com.rioni.lk.api.service;

import com.rioni.lk.api.dto.SubaccountAssetsResponse;

public interface SubaccountAssetService {
    SubaccountAssetsResponse getAllAssetsByProfileId(Long profileId, String assetTypeCode);
}