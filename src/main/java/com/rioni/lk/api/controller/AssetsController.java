package com.rioni.lk.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

import com.rioni.lk.api.service.SubaccountAssetService;
import com.rioni.lk.api.dto.SubaccountAssetsResponse;

@RestController
@RequestMapping("/api/")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class AssetsController {
    
    private final SubaccountAssetService subaccountAssetService;

     @Autowired
     public AssetsController(SubaccountAssetService subaccountAssetService) {
         this.subaccountAssetService = subaccountAssetService;
     }

     private Long getCurrentProfileId() {
         Integer profileId = (Integer) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
         return profileId.longValue();
     }

     @GetMapping("/portfolio/assets")
     public ResponseEntity<SubaccountAssetsResponse> getAssets(
             @RequestParam(required = false) String types,
             @RequestParam(required = false, defaultValue = "1") int page,
             @RequestParam(required = false, defaultValue = "10") int perPage,
             @RequestParam(required = false) String search) {
        Long profileId = getCurrentProfileId();
        SubaccountAssetsResponse assets = subaccountAssetService.getAllAssetsByProfileId(profileId, types, page, perPage, search);
        return new ResponseEntity<>(assets, HttpStatus.OK);
     }
}