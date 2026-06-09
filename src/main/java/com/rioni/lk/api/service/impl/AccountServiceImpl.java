package com.rioni.lk.api.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import com.rioni.lk.api.dto.AccountResponse;
import com.rioni.lk.api.dto.AccountDto;
import com.rioni.lk.api.service.AccountService;
import com.rioni.lk.api.repository.AccountRepository;
import com.rioni.lk.api.repository.SubaccountRepository;
import com.rioni.lk.api.repository.SubaccountValueRepository;
import com.rioni.lk.api.model.Subaccount;
import com.rioni.lk.api.model.SubaccountValue;
import org.springframework.stereotype.Service;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.math.BigDecimal;

@Service
public class AccountServiceImpl implements AccountService {
    private final AccountRepository accountRepository;
    private final SubaccountRepository subaccountRepository;
    private final SubaccountValueRepository subaccountValueRepository;

    @Autowired
    public AccountServiceImpl(AccountRepository accountRepository,
                              SubaccountRepository subaccountRepository,
                              SubaccountValueRepository subaccountValueRepository) {
        this.accountRepository = accountRepository;
        this.subaccountRepository = subaccountRepository;
        this.subaccountValueRepository = subaccountValueRepository;
    }

    @Override
    public AccountResponse getAllAccountsByProfileId(Long profileId) {
        AccountResponse response = new AccountResponse();
        List<AccountDto> accounts = accountRepository.findByProfileId(profileId.intValue()).stream()
                .map(acc -> {
                    AccountDto dto = new AccountDto(acc);
                    calculateAccountSums(dto);
                    return dto;
                })
                .collect(Collectors.toList());

        response.setAccounts(accounts);
        return response;
    }

    private void calculateAccountSums(AccountDto dto) {
        List<Subaccount> subaccounts = subaccountRepository.findByAccountId(dto.getId());

        Map<Integer, Subaccount> subaccountMap = subaccounts.stream()
                .collect(Collectors.toMap(Subaccount::getId, Function.identity()));

        List<SubaccountValue> values = subaccountValueRepository.findAll().stream()
                .filter(sv -> subaccountMap.containsKey(sv.getSubaccountId()))
                .collect(Collectors.toList());

        Map<Integer, SubaccountValue> latestValues = values.stream()
                .collect(Collectors.toMap(
                        SubaccountValue::getSubaccountId,
                        sv -> sv,
                        (v1, v2) -> v1.getDate().compareTo(v2.getDate()) > 0 ? v1 : v2
                ));

        BigDecimal balance = BigDecimal.ZERO;
        BigDecimal deposit = BigDecimal.ZERO;
        BigDecimal fundsInTransit = BigDecimal.ZERO;
        BigDecimal tradingFunds = BigDecimal.ZERO;

        for (Subaccount subaccount : subaccounts) {
            SubaccountValue sv = latestValues.get(subaccount.getId());
            if (sv != null && sv.getBalanceValue() != null) {
                BigDecimal value = sv.getBalanceValue();
                balance = balance.add(value);
                String typeCode = subaccount.getSubaccountTypeCode();
                if ("D".equals(typeCode)) {
                    deposit = deposit.add(value);
                } else if ("W".equals(typeCode)) {
                    fundsInTransit = fundsInTransit.add(value);
                } else if ("T".equals(typeCode)) {
                    tradingFunds = tradingFunds.add(value);
                }
            }
        }

        dto.setBalance(balance);
        dto.setDeposit(deposit);
        dto.setFundsInTransit(fundsInTransit);
        dto.setTradingFunds(tradingFunds);
    }
}