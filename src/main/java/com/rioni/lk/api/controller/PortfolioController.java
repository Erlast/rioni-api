package com.rioni.lk.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

import com.rioni.lk.api.service.AccountService;
import com.rioni.lk.api.dto.AccountResponse;

@RestController
@RequestMapping("/api/")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class PortfolioController {
    
    private final AccountService accountService;

    @Autowired
    public PortfolioController(AccountService accountService) {
        this.accountService = accountService;
    }

     @GetMapping("/portfolios/{profileId}")
    public ResponseEntity<AccountResponse> getAccounts(@PathVariable Long profileId) {
        AccountResponse accounts = accountService.getAllAccountsByProfileId(profileId);
        return new ResponseEntity<>(accounts, HttpStatus.OK);
    }
}
