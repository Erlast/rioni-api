package com.rioni.lk.api.service;

import com.rioni.lk.api.dto.ProfileDto;
import com.rioni.lk.api.dto.ProfileResponseDto;
import com.rioni.lk.api.dto.ProfileContactDto;
import com.rioni.lk.api.dto.ProfileAddressDto;
import java.util.List;

public interface ProfileService {

    ProfileResponseDto getProfileById(Long id);

    ProfileResponseDto saveProfile(Long id, ProfileDto profileDto);

    boolean saveContacts(Long profileId, List<ProfileContactDto> contacts);

    boolean saveAddresses(Long profileId, List<ProfileAddressDto> addresses);

}