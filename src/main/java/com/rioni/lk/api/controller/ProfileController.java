package com.rioni.lk.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rioni.lk.api.service.ProfileService;
import com.rioni.lk.api.dto.ProfileDto;
import com.rioni.lk.api.dto.ProfileResponseDto;
import com.rioni.lk.api.dto.ProfileContactDto;
import com.rioni.lk.api.dto.ProfileAddressDto;
import com.rioni.lk.api.dto.TaxResidenceDto;
import com.rioni.lk.api.dto.ResidencePermitDto;
import com.rioni.lk.api.dto.BankAccountDto;
import java.util.List;
import java.util.UUID;
import java.io.IOException;
import java.util.Base64;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ProfileController {
    private final ProfileService profileService;

    @Autowired
    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/profile/{userId}")
    public ResponseEntity<ProfileResponseDto> getProfile(@PathVariable Long userId) {
        ProfileResponseDto profile = profileService.getProfileById(userId);
        if (profile == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(profile, HttpStatus.OK);
    }

    @PatchMapping("/profile/{userId}")
    public ResponseEntity<ProfileResponseDto> saveProfile(@PathVariable Long userId, @RequestBody ProfileDto profileDto) {
        ProfileResponseDto saved = profileService.saveProfile(userId, profileDto);
        if (saved == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(saved, HttpStatus.OK);
    }

    @PutMapping("/profile/{userId}/contacts")
    public ResponseEntity<Void> saveContacts(@PathVariable Long userId, @RequestBody List<ProfileContactDto> contacts) {
        boolean result = profileService.saveContacts(userId, contacts);
        if (!result) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping("/profile/{userId}/addresses")
    public ResponseEntity<Void> saveAddresses(@PathVariable Long userId, @RequestBody List<ProfileAddressDto> addresses) {
        boolean result = profileService.saveAddresses(userId, addresses);
        if (!result) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/profile/{userId}/tax-residences")
    public ResponseEntity<List<TaxResidenceDto>> getTaxResidences(@PathVariable Long userId) {
        List<TaxResidenceDto> taxResidences = profileService.getTaxResidences(userId);
        return new ResponseEntity<>(taxResidences, HttpStatus.OK);
    }

    @GetMapping("/profile/{userId}/residence-permits")
    public ResponseEntity<List<ResidencePermitDto>> getResidencePermits(@PathVariable Long userId) {
        List<ResidencePermitDto> residencePermits = profileService.getResidencePermits(userId);
        return new ResponseEntity<>(residencePermits, HttpStatus.OK);
    }

    @PutMapping("/profile/{userId}/tax-residences")
    public ResponseEntity<Void> saveTaxResidences(@PathVariable Long userId, @RequestBody List<TaxResidenceDto> taxResidences) {
        boolean result = profileService.saveTaxResidences(userId, taxResidences);
        if (!result) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping("/profile/{userId}/residence-permits")
    public ResponseEntity<Void> saveResidencePermits(@PathVariable Long userId, @RequestBody List<ResidencePermitDto> residencePermits) {
        boolean result = profileService.saveResidencePermits(userId, residencePermits);
        if (!result) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/profile/{userId}/bank-accounts")
    public ResponseEntity<List<BankAccountDto>> getBankAccounts(@PathVariable Long userId) {
        List<BankAccountDto> bankAccounts = profileService.getBankAccounts(userId);
        return new ResponseEntity<>(bankAccounts, HttpStatus.OK);
    }

    @PutMapping("/profile/{userId}/bank-accounts")
    public ResponseEntity<Void> saveBankAccounts(@PathVariable Long userId, @RequestBody List<BankAccountDto> bankAccounts) {
        boolean result = profileService.saveBankAccounts(userId, bankAccounts);
        if (!result) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping(value = "/profile/{userId}/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> saveAvatar(@PathVariable Long userId, @RequestParam("file") MultipartFile file) {
        try {
            String contentType = file.getContentType();
            if (contentType == null) {
                contentType = "image/jpeg";
            }
            String extension = "";
            String originalFilename = file.getOriginalFilename();
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
                contentType = switch (extension) {
                    case ".png" -> "image/png";
                    case ".gif" -> "image/gif";
                    case ".webp" -> "image/webp";
                    default -> "image/jpeg";
                };
            }

            byte[] imageBytes = file.getBytes();
            String base64Image = "data:" + contentType + ";base64," + Base64.getEncoder().encodeToString(imageBytes);

            boolean result = profileService.saveAvatar(userId, base64Image);
            if (!result) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(Map.of("url", base64Image), HttpStatus.OK);
        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}