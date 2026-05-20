package com.rioni.lk.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import com.rioni.lk.api.service.AccountService;
import com.rioni.lk.api.service.PortfolioService;
import com.rioni.lk.api.dto.AccountResponse;
import com.rioni.lk.api.dto.AccountYieldDto;
import com.rioni.lk.api.dto.PortfolioValueDto;
import com.rioni.lk.api.dto.Timeframe;

@RestController
@RequestMapping("/api/")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class PortfolioController {

    private final AccountService accountService;
    private final PortfolioService portfolioService;

    @Autowired
    public PortfolioController(AccountService accountService, PortfolioService portfolioService) {
        this.accountService = accountService;
        this.portfolioService = portfolioService;
    }

    @GetMapping("/portfolios/{profileId}")
    public ResponseEntity<AccountResponse> getAccounts(@PathVariable Long profileId) {
        AccountResponse accounts = accountService.getAllAccountsByProfileId(profileId);
        return new ResponseEntity<>(accounts, HttpStatus.OK);
    }

    @GetMapping("/portfolio/{accountId}")
    public ResponseEntity<List<PortfolioValueDto>> getPortfolioValues(
            @PathVariable Integer accountId,
            @RequestParam(required = false, defaultValue = "month") String timeframe) {
        Timeframe tf = Timeframe.fromValue(timeframe);
        List<PortfolioValueDto> portfolioValues = portfolioService.getPortfolioValuesByAccountId(accountId, tf);
        return new ResponseEntity<>(portfolioValues, HttpStatus.OK);
    }

    @GetMapping("/portfolio/{accountId}/yield")
    public ResponseEntity<AccountYieldDto> getAccountYield(
            @PathVariable Integer accountId,
            @RequestParam(required = false, defaultValue = "day") String period) {
        Timeframe timeframe = Timeframe.fromValue(period);
        AccountYieldDto yield = portfolioService.getAccountYield(accountId, timeframe);
        return new ResponseEntity<>(yield, HttpStatus.OK);
    }
}
