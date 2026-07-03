package com.rioni.lk.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubaccountAssetsResponse {
    private List<SubaccountAssetDto> assets;
    private ProfitDto profit;
    private int page;
    private int perPage;
    private long total;
    private int totalPages;
}
