package com.rioni.lk.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
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

    private Long getCurrentProfileId() {
        Integer profileId = (Integer) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return profileId.longValue();
    }

    @GetMapping("/profile/me")
    public ResponseEntity<ProfileResponseDto> getProfile() {
        Long profileId = getCurrentProfileId();
        ProfileResponseDto profile = profileService.getProfileById(profileId);
        if (profile == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(profile, HttpStatus.OK);
    }

    @PatchMapping("/profile/me")
    public ResponseEntity<ProfileResponseDto> saveProfile(@RequestBody ProfileDto profileDto) {
        Long profileId = getCurrentProfileId();
        ProfileResponseDto saved = profileService.saveProfile(profileId, profileDto);
        if (saved == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(saved, HttpStatus.OK);
    }

    @PutMapping("/profile/me/contacts")
    public ResponseEntity<Void> saveContacts(@RequestBody List<ProfileContactDto> contacts) {
        Long profileId = getCurrentProfileId();
        boolean result = profileService.saveContacts(profileId, contacts);
        if (!result) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping("/profile/me/addresses")
    public ResponseEntity<Void> saveAddresses(@RequestBody List<ProfileAddressDto> addresses) {
        Long profileId = getCurrentProfileId();
        boolean result = profileService.saveAddresses(profileId, addresses);
        if (!result) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/profile/me/tax-residences")
    public ResponseEntity<List<TaxResidenceDto>> getTaxResidences() {
        Long profileId = getCurrentProfileId();
        List<TaxResidenceDto> taxResidences = profileService.getTaxResidences(profileId);
        return new ResponseEntity<>(taxResidences, HttpStatus.OK);
    }

    @GetMapping("/profile/me/residence-permits")
    public ResponseEntity<List<ResidencePermitDto>> getResidencePermits() {
        Long profileId = getCurrentProfileId();
        List<ResidencePermitDto> residencePermits = profileService.getResidencePermits(profileId);
        return new ResponseEntity<>(residencePermits, HttpStatus.OK);
    }

    @PutMapping("/profile/me/tax-residences")
    public ResponseEntity<Void> saveTaxResidences(@RequestBody List<TaxResidenceDto> taxResidences) {
        Long profileId = getCurrentProfileId();
        boolean result = profileService.saveTaxResidences(profileId, taxResidences);
        if (!result) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping("/profile/me/residence-permits")
    public ResponseEntity<Void> saveResidencePermits(@RequestBody List<ResidencePermitDto> residencePermits) {
        Long profileId = getCurrentProfileId();
        boolean result = profileService.saveResidencePermits(profileId, residencePermits);
        if (!result) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/profile/me/bank-accounts")
    public ResponseEntity<List<BankAccountDto>> getBankAccounts() {
        Long profileId = getCurrentProfileId();
        List<BankAccountDto> bankAccounts = profileService.getBankAccounts(profileId);
        return new ResponseEntity<>(bankAccounts, HttpStatus.OK);
    }

    @PutMapping("/profile/me/bank-accounts")
    public ResponseEntity<Void> saveBankAccounts(@RequestBody List<BankAccountDto> bankAccounts) {
        Long profileId = getCurrentProfileId();
        boolean result = profileService.saveBankAccounts(profileId, bankAccounts);
        if (!result) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping(value = "/profile/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> saveAvatar(@RequestParam("file") MultipartFile file) {
        Long profileId = getCurrentProfileId();
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

            boolean result = profileService.saveAvatar(profileId, base64Image);
            if (!result) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(Map.of("url", base64Image), HttpStatus.OK);
        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}