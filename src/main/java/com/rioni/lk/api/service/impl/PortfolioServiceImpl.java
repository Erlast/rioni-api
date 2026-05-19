package com.rioni.lk.api.service.impl;

import com.rioni.lk.api.dto.PortfolioValueDto;
import com.rioni.lk.api.dto.Timeframe;
import com.rioni.lk.api.repository.SubaccountValueRepository;
import com.rioni.lk.api.service.PortfolioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PortfolioServiceImpl implements PortfolioService {

    private final SubaccountValueRepository subaccountValueRepository;

    @Autowired
    public PortfolioServiceImpl(SubaccountValueRepository subaccountValueRepository) {
        this.subaccountValueRepository = subaccountValueRepository;
    }

    @Override
    public List<PortfolioValueDto> getPortfolioValuesByAccountId(Integer accountId, Timeframe timeframe) {
        LocalDate startDate = calculateStartDate(timeframe);
        List<Object[]> results = subaccountValueRepository.findGroupedByDateByAccountId(accountId, startDate.toString());

        return results.stream()
                .map(row -> {
                    String dateStr = (String) row[0];
                    BigDecimal value = (BigDecimal) row[1];
                    long timestamp = LocalDate.parse(dateStr).atStartOfDay().toEpochSecond(ZoneOffset.UTC);
                    return new PortfolioValueDto(timestamp, value);
                })
                .collect(Collectors.toList());
    }

    private LocalDate calculateStartDate(Timeframe timeframe) {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        
        switch (timeframe) {
            case MONTH:
                return today.minus(1, ChronoUnit.MONTHS);
            case SIX_MONTHS:
                return today.minus(6, ChronoUnit.MONTHS);
            case FROM_YEAR:
                return LocalDate.of(today.getYear(), 1, 1);
            case YEAR:
                return today.minus(1, ChronoUnit.YEARS);
            case ALL_PERIOD:
                return today.minus(2, ChronoUnit.YEARS);
            case WEEK:
            default:
                return today.minus(7, ChronoUnit.DAYS);


        }
    }
}