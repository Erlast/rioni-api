package com.rioni.lk.api.service.impl;

import com.rioni.lk.api.dto.CurrencyDto;
import com.rioni.lk.api.repository.CurrencyRepository;
import com.rioni.lk.api.service.CurrencyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CurrencyServiceImpl implements CurrencyService {

    private final CurrencyRepository currencyRepository;

    @Autowired
    public CurrencyServiceImpl(CurrencyRepository currencyRepository) {
        this.currencyRepository = currencyRepository;
    }

    @Override
    public List<CurrencyDto> getAllCurrencies() {
        return currencyRepository.findAll().stream()
                .map(CurrencyDto::new)
                .collect(Collectors.toList());
    }
}
