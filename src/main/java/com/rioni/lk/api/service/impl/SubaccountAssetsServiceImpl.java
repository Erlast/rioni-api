package com.rioni.lk.api.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import com.rioni.lk.api.dto.SubaccountAssetsResponse;
import com.rioni.lk.api.dto.SubaccountAssetDto;
import com.rioni.lk.api.dto.ProfitDto;
import com.rioni.lk.api.service.SubaccountAssetService;
import com.rioni.lk.api.repository.SubaccountAssetRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SubaccountAssetsServiceImpl implements SubaccountAssetService {
    private final SubaccountAssetRepository subaccountAssetRepository;

    @Autowired
    public SubaccountAssetsServiceImpl(SubaccountAssetRepository subaccountAssetRepository) {
        this.subaccountAssetRepository = subaccountAssetRepository;
    }

    @Override
    public SubaccountAssetsResponse getAllAssetsByProfileId(Long profileId, String assetTypeCode) {
        List<SubaccountAssetDto> assets = ((List<Object[]>) subaccountAssetRepository.findByProfileId(profileId.intValue(), assetTypeCode)).stream()
                .map(row -> new SubaccountAssetDto(row))
                .collect(Collectors.toList());

        int balance = assets.stream().mapToInt(SubaccountAssetDto::getBalanceValue).sum();
        int invested = assets.stream().mapToInt(dto -> dto.getInvestedValue().intValue()).sum();
        int totalDiff = balance - invested;

        SubaccountAssetsResponse response = new SubaccountAssetsResponse();
        response.setAssets(assets);
        response.setProfit(new ProfitDto(invested, totalDiff));
        return response;
    }
}
