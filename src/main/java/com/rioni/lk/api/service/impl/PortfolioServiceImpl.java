package com.rioni.lk.api.service.impl;

import com.rioni.lk.api.dto.AccountYieldDto;
import com.rioni.lk.api.dto.PortfolioValueDto;
import com.rioni.lk.api.dto.Timeframe;
import com.rioni.lk.api.repository.SubaccountValueRepository;
import com.rioni.lk.api.service.PortfolioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
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

    @Override
    public AccountYieldDto getAccountYield(Integer accountId, Timeframe period) {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        LocalDate currentPeriodEnd = today;
        LocalDate currentPeriodStart = calculatePeriodStartDate(currentPeriodEnd, period);
        LocalDate previousPeriodEnd = currentPeriodStart.minusDays(1);
        LocalDate previousPeriodStart = calculatePeriodStartDate(previousPeriodEnd, period);

        List<Object[]> currentResults = subaccountValueRepository.findGroupedByDateByAccountId(
                accountId, currentPeriodStart.toString());
        List<Object[]> previousResults = subaccountValueRepository.findGroupedByDateByAccountId(
                accountId, previousPeriodStart.toString());

        Optional<BigDecimal> currentBalance = findBalanceForPeriod(currentResults, currentPeriodStart, currentPeriodEnd);
        Optional<BigDecimal> previousBalance = findBalanceForPeriod(previousResults, previousPeriodStart, previousPeriodEnd);

        if (currentBalance.isEmpty() || previousBalance.isEmpty()) {
            return new AccountYieldDto(BigDecimal.ZERO, BigDecimal.ZERO);
        }

        BigDecimal currentVal = currentBalance.get();
        BigDecimal previousVal = previousBalance.get();

        BigDecimal accountYield = currentVal.subtract(previousVal);

        BigDecimal accountPercent = BigDecimal.ZERO;
        if (previousVal.abs().compareTo(BigDecimal.ZERO) != 0) {
            accountPercent = accountYield
                    .divide(previousVal.abs(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        return new AccountYieldDto(accountYield, accountPercent);
    }

    private Optional<BigDecimal> findBalanceForPeriod(List<Object[]> results, LocalDate periodStart, LocalDate periodEnd) {
        return results.stream()
                .filter(row -> {
                    String dateStr = (String) row[0];
                    LocalDate date = LocalDate.parse(dateStr);
                    return !date.isBefore(periodStart) && !date.isAfter(periodEnd);
                })
                .map(row -> (BigDecimal) row[1])
                .reduce((a, b) -> a.add(b));
    }

    private LocalDate calculatePeriodStartDate(LocalDate endDate, Timeframe period) {
        switch (period) {
            case DAY:
                return endDate;
            case WEEK:
                return endDate.minus(7, ChronoUnit.DAYS);
            case MONTH:
                return endDate.minus(1, ChronoUnit.MONTHS);
            case YEAR:
                return endDate.minus(1, ChronoUnit.YEARS);
            case ALL_PERIOD:
                return LocalDate.of(2000, 1, 1);
            case SIX_MONTHS:
                return endDate.minus(6, ChronoUnit.MONTHS);
            case FROM_YEAR:
            default:
                return LocalDate.of(endDate.getYear(), 1, 1);
        }
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