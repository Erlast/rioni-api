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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rioni.lk.api.service.ProfileService;
import com.rioni.lk.api.dto.ProfileDto;
import com.rioni.lk.api.dto.ProfileResponseDto;
import com.rioni.lk.api.dto.ProfileContactDto;
import com.rioni.lk.api.dto.ProfileAddressDto;
import java.util.List;

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

}