package com.rioni.lk.api.controller;

import com.rioni.lk.api.dto.CurrencyRatesResponse;
import com.rioni.lk.api.service.NbgCurrencyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/")
public class NbgCurrencyController {

    private final NbgCurrencyService nbgCurrencyService;

    @Autowired
    public NbgCurrencyController(NbgCurrencyService nbgCurrencyService) {
        this.nbgCurrencyService = nbgCurrencyService;
    }

    @GetMapping("currencies/nbg-rates")
    public ResponseEntity<CurrencyRatesResponse> getNbgRates() {
        return new ResponseEntity<>(nbgCurrencyService.getRates(), HttpStatus.OK);
    }
}
