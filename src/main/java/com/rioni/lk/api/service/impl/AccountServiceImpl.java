package com.rioni.lk.api.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import com.rioni.lk.api.dto.AccountResponse;
import com.rioni.lk.api.dto.AccountDto;
import com.rioni.lk.api.service.AccountService;
import com.rioni.lk.api.repository.AccountRepository;
import org.springframework.stereotype.Service;
import java.util.stream.Collectors;

@Service
public class AccountServiceImpl implements AccountService {
    private final AccountRepository accountRepository;

    @Autowired
    public AccountServiceImpl(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public AccountResponse getAllAccountsByProfileId(Long profileId) {
        AccountResponse response = new AccountResponse();
        response.setData(
                accountRepository.findAll().stream()
                        .filter(acc -> acc.getProfileId() == profileId)
                        .map(AccountDto::new)
                        .collect(Collectors.toList())
        );
        return response;
    }
}
