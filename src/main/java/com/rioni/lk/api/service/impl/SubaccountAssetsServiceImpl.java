package com.rioni.lk.api.service.impl;

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

    public SubaccountAssetsServiceImpl(SubaccountAssetRepository subaccountAssetRepository) {
        this.subaccountAssetRepository = subaccountAssetRepository;
    }

    @Override
    public SubaccountAssetsResponse getAllAssetsByProfileId(Long profileId,
                                                            String assetTypeCode,
                                                            int page,
                                                            int perPage,
                                                            String search) {
        List<SubaccountAssetDto> allAssets = subaccountAssetRepository
                .findAssetsByProfileId(profileId.intValue(), assetTypeCode, search)
                .stream()
                .map(SubaccountAssetDto::new)
                .collect(Collectors.toList());

        long total = subaccountAssetRepository.countAssetsByProfileId(
                profileId.intValue(), assetTypeCode, search);

        return buildResponse(allAssets, total, page, perPage);
    }

    @Override
    public SubaccountAssetsResponse getAllAssetsByAccountId(Integer accountId,
                                                            String assetTypeCode,
                                                            int page,
                                                            int perPage,
                                                            String search) {
        List<SubaccountAssetDto> allAssets = subaccountAssetRepository
                .findAssetsByAccountId(accountId, assetTypeCode, search)
                .stream()
                .map(SubaccountAssetDto::new)
                .collect(Collectors.toList());

        long total = subaccountAssetRepository.countAssetsByAccountId(
                accountId, assetTypeCode, search);

        return buildResponse(allAssets, total, page, perPage);
    }

    private SubaccountAssetsResponse buildResponse(List<SubaccountAssetDto> allAssets,
                                                    long total,
                                                    int page,
                                                    int perPage) {
        int totalPages = (int) Math.ceil((double) total / perPage);
        if (totalPages == 0) totalPages = 1;
        if (page < 1) page = 1;
        if (page > totalPages) page = totalPages;

        int fromIndex = (page - 1) * perPage;
        int toIndex = Math.min(fromIndex + perPage, allAssets.size());

        List<SubaccountAssetDto> pagedAssets;
        if (fromIndex >= allAssets.size()) {
            pagedAssets = List.of();
        } else {
            pagedAssets = allAssets.subList(fromIndex, toIndex);
        }

        int balance = pagedAssets.stream().mapToInt(SubaccountAssetDto::getBalanceValue).sum();
        int invested = pagedAssets.stream()
                .mapToInt(dto -> dto.getInvestedValue().intValue())
                .sum();
        int totalDiff = balance - invested;

        SubaccountAssetsResponse response = new SubaccountAssetsResponse();
        response.setAssets(pagedAssets);
        response.setProfit(new ProfitDto(invested, totalDiff));
        response.setPage(page);
        response.setPerPage(perPage);
        response.setTotal(total);
        response.setTotalPages(totalPages);
        return response;
    }
}
