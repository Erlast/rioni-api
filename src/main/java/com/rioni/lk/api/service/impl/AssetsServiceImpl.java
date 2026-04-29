package com.rioni.lk.api.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import com.rioni.lk.api.dto.AssetsResponse;
import com.rioni.lk.api.dto.AssetDto;
import com.rioni.lk.api.service.AssetsService;
import com.rioni.lk.api.repository.AssetRepository;
import org.springframework.stereotype.Service;
import java.util.stream.Collectors;

@Service
public class AssetsServiceImpl implements AssetsService {
    private final AssetRepository assetRepository;

    @Autowired
    public AssetsServiceImpl(AssetRepository assetRepository) {
        this.assetRepository = assetRepository;
    }

    @Override
    public AssetsResponse getAllAssetsByProfileId(Long profileId) {
        AssetsResponse response = new AssetsResponse();
        response.setData(
                assetRepository.findAll().stream()
                        .map(AssetDto::new)
                        .collect(Collectors.toList())
        );
        return response;
    }
}