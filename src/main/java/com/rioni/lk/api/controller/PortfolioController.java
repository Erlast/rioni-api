package com.rioni.lk.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import com.rioni.lk.api.service.AccountService;
import com.rioni.lk.api.service.PortfolioService;
import com.rioni.lk.api.service.SubaccountAssetService;
import com.rioni.lk.api.dto.AccountResponse;
import com.rioni.lk.api.dto.AccountYieldDto;
import com.rioni.lk.api.dto.PortfolioValueDto;
import com.rioni.lk.api.dto.SubaccountAssetsResponse;
import com.rioni.lk.api.dto.Timeframe;

@RestController
@RequestMapping("/api/")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class PortfolioController {

    private final AccountService accountService;
    private final PortfolioService portfolioService;
    private final SubaccountAssetService subaccountAssetService;

    @Autowired
    public PortfolioController(AccountService accountService, PortfolioService portfolioService,
                               SubaccountAssetService subaccountAssetService) {
        this.accountService = accountService;
        this.portfolioService = portfolioService;
        this.subaccountAssetService = subaccountAssetService;
    }

    private Long getCurrentProfileId() {
        Integer profileId = (Integer) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return profileId.longValue();
    }

    @GetMapping("/portfolios")
    public ResponseEntity<AccountResponse> getAccounts() {
        Long profileId = getCurrentProfileId();
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

    @GetMapping("/portfolio/{accountId}/assets")
    public ResponseEntity<SubaccountAssetsResponse> getAccountAssets(
            @PathVariable Integer accountId,
            @RequestParam(required = false) String types,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int perPage,
            @RequestParam(required = false) String search) {
        SubaccountAssetsResponse assets = subaccountAssetService.getAllAssetsByAccountId(accountId, types, page, perPage, search);
        return new ResponseEntity<>(assets, HttpStatus.OK);
    }
}
