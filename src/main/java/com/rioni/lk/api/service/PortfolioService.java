package com.rioni.lk.api.service;

import com.rioni.lk.api.dto.PortfolioValueDto;
import com.rioni.lk.api.dto.Timeframe;
import java.util.List;

public interface PortfolioService {
    List<PortfolioValueDto> getPortfolioValuesByAccountId(Integer accountId, Timeframe timeframe);
}