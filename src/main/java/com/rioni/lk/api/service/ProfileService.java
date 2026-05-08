package com.rioni.lk.api.service;

import com.rioni.lk.api.dto.ProfileDto;
import com.rioni.lk.api.dto.ProfileResponseDto;

public interface ProfileService {

    ProfileResponseDto getProfileById(Long id);

    ProfileResponseDto saveProfile(Long id, ProfileDto profileDto);

}