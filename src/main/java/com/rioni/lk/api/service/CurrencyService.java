package com.rioni.lk.api.service;

import com.rioni.lk.api.dto.CurrencyDto;
import java.util.List;

public interface CurrencyService {
    List<CurrencyDto> getAllCurrencies();
}
