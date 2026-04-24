package com.rioni.lk.api.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import com.rioni.lk.api.dto.ProfileDto;
import com.rioni.lk.api.service.ProfileService;
import com.rioni.lk.api.repository.ProfileRepository;
import com.rioni.lk.api.repository.AccountRepository;
import com.rioni.lk.api.mapper.ProfileMapper;
import com.rioni.lk.api.dto.AccountDto;
import org.springframework.stereotype.Service;
import java.util.stream.Collectors;

@Service
public class ProfileServiceImpl implements ProfileService {
    private final ProfileRepository profileRepository;
    private final AccountRepository accountRepository;

    @Autowired
    public ProfileServiceImpl(ProfileRepository profileRepository, AccountRepository accountRepository) {
        this.profileRepository = profileRepository;
        this.accountRepository = accountRepository;
    }

    @Override
    public ProfileDto getProfileById(Long id) {
        return profileRepository.findById(id)
                .map(profile -> {
                    ProfileDto dto = ProfileMapper.mapToDto(profile);
                    dto.setAccounts(
                            accountRepository.findAll().stream()
                                    .filter(acc -> acc.getProfileId() == profile.getId())
                                    .map(AccountDto::new)
                                    .collect(Collectors.toList())
                    );
                    return dto;
                })
                .orElse(null);
    }
}