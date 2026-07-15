package com.rioni.lk.api.repository;

import com.querydsl.core.Tuple;

import java.util.List;

public interface SubaccountAssetRepositoryCustom {

    List<Tuple> findAssetsByProfileId(Integer profileId, String assetTypeCode, String search);

    long countAssetsByProfileId(Integer profileId, String assetTypeCode, String search);

    List<Tuple> findAssetsByAccountId(Integer accountId, String assetTypeCode, String search);

    long countAssetsByAccountId(Integer accountId, String assetTypeCode, String search);
}
