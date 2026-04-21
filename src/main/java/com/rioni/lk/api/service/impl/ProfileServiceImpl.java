package com.rioni.lk.api.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import com.rioni.lk.api.dto.ProfileDto;
import com.rioni.lk.api.service.ProfileService;
import com.rioni.lk.api.repository.ProfileRepository;
import com.rioni.lk.api.mapper.ProfileMapper;
import org.springframework.stereotype.Service;

@Service
public class ProfileServiceImpl implements ProfileService {
    private final ProfileRepository profileRepository;

    @Autowired
    public ProfileServiceImpl(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    @Override
    public ProfileDto getProfileById(Long id) {
        return profileRepository.findById(id)
                .map(ProfileMapper::mapToDto)
                .orElse(null);
    }
}