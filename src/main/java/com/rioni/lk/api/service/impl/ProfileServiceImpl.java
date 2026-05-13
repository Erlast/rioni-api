package com.rioni.lk.api.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import com.rioni.lk.api.dto.ProfileDto;
import com.rioni.lk.api.dto.ProfileResponseDto;
import com.rioni.lk.api.dto.ProfileContactDto;
import com.rioni.lk.api.dto.ProfileAddressDto;
import com.rioni.lk.api.service.ProfileService;
import com.rioni.lk.api.repository.ProfileRepository;
import com.rioni.lk.api.repository.ProfileContactRepository;
import com.rioni.lk.api.repository.ProfileAddressRepository;
import com.rioni.lk.api.mapper.ProfileMapper;
import com.rioni.lk.api.model.ProfileContact;
import com.rioni.lk.api.model.ProfileAddress;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class ProfileServiceImpl implements ProfileService {
    private final ProfileRepository profileRepository;
    private final ProfileContactRepository profileContactRepository;
    private final ProfileAddressRepository profileAddressRepository;

    @Autowired
    public ProfileServiceImpl(ProfileRepository profileRepository, ProfileContactRepository profileContactRepository, ProfileAddressRepository profileAddressRepository) {
        this.profileRepository = profileRepository;
        this.profileContactRepository = profileContactRepository;
        this.profileAddressRepository = profileAddressRepository;
    }

    @Override
    public ProfileResponseDto getProfileById(Long id) {
        return profileRepository.findById(id)
                .map(profile -> {
                    List<ProfileContact> contacts = profileContactRepository.findByProfileId(profile.getId());
                    List<ProfileAddress> addresses = profileAddressRepository.findByProfileId(profile.getId());
                    return ProfileMapper.mapToDto(profile, contacts, addresses);
                })
                .orElse(null);
    }

    @Override
    public ProfileResponseDto saveProfile(Long id, ProfileDto profileDto) {
        return profileRepository.findById(id)
                .map(profile -> {
                    ProfileMapper.mapToEntity(profileDto, profile);
                    return ProfileMapper.mapToDto(profileRepository.save(profile), List.of(), List.of());
                })
                .orElse(null);
    }

    @Override
    @Transactional
    public boolean saveContacts(Long profileId, List<ProfileContactDto> contacts) {
        if (!profileRepository.existsById(profileId)) {
            return false;
        }
        for (ProfileContactDto contactDto : contacts) {
            ProfileContact contact = new ProfileContact();
            contact.setProfileId(profileId.intValue());
            contact.setContactType(contactDto.getContactType());
            contact.setIsMain(contactDto.getIsMain());
            contact.setValue(contactDto.getValue());
            contact.setIsConfirmed(contactDto.getIsConfirmed());
            if (contactDto.getId() != 0) {
                contact.setId(contactDto.getId());
            }
            profileContactRepository.save(contact);
        }
        return true;
    }

    @Override
    @Transactional
    public boolean saveAddresses(Long profileId, List<ProfileAddressDto> addresses) {
        if (!profileRepository.existsById(profileId)) {
            return false;
        }
        for (ProfileAddressDto addressDto : addresses) {
            ProfileAddress address = new ProfileAddress();
            address.setProfileId(profileId.intValue());
            address.setCountry(addressDto.getCountry());
            address.setCity(addressDto.getCity());
            address.setPostcode(addressDto.getPostcode());
            address.setAddress(addressDto.getAddress());
            address.setIsMain(addressDto.getIsMain());
            address.setIsConfirmed(addressDto.getIsConfirmed());
            address.setAddressType(addressDto.getAddressType());
            if (addressDto.getId() != 0) {
                address.setId(addressDto.getId());
            }
            profileAddressRepository.save(address);
        }
        return true;
    }
}