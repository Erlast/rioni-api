package com.rioni.lk.api.service;

import com.rioni.lk.api.dto.AssetsResponse;

public interface AssetsService {
    AssetsResponse getAllAssetsByProfileId(Long profileId);
}
