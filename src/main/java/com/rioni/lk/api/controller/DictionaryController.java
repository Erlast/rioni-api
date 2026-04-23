package com.rioni.lk.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rioni.lk.api.service.CurrencyService;
import com.rioni.lk.api.dto.CurrencyDto;
import com.rioni.lk.api.dto.DictionariesResponse;
import java.util.List;

@RestController
@RequestMapping("/api/")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class DictionaryController {

    private final CurrencyService currencyService;

    @Autowired
    public DictionaryController(CurrencyService currencyService) {
        this.currencyService = currencyService;
    }

    @GetMapping("/dictionaries/")
    public ResponseEntity<DictionariesResponse> getDictionaries() {
        List<CurrencyDto> currencies = currencyService.getAllCurrencies();
        return new ResponseEntity<>(new DictionariesResponse(currencies), HttpStatus.OK);
    }
}
