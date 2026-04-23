package com.rioni.lk.api.mapper;

import lombok.NoArgsConstructor;
import com.rioni.lk.api.dto.ProfileDto;
import com.rioni.lk.api.model.Profile;

@NoArgsConstructor
public class ProfileMapper {
    public static ProfileDto mapToDto(Profile profile) {
        ProfileDto profileDto = new ProfileDto();
        profileDto.setId(profile.getId());
        profileDto.setNbs(profile.getNbs());
        profileDto.setNdu(profile.getNdu());
        profileDto.setName(profile.getName());
        profileDto.setSurname(profile.getSurname());
        profileDto.setPatronymic(profile.getPatronymic());
        profileDto.setPhotoUrl(profile.getPhotoUrl());
        profileDto.setEmail(profile.getEmail());
        profileDto.setPhone(profile.getPhone());
        profileDto.setDateOfBirth(profile.getDateOfBirth());
        profileDto.setPlaceOfBirth(profile.getPlaceOfBirth());
        profileDto.setGender(profile.getGender());
        profileDto.setCitizenship(profile.getCitizenship());
        profileDto.setPlaceOfBirth(profile.getPlaceOfBirth());
        profileDto.setDocumentType(profile.getDocumentType());
        profileDto.setPassportNumber(profile.getPassportNumber());
        profileDto.setPassportIssueDate(profile.getPassportIssueDate());
        profileDto.setPassportExpiryDate(profile.getPassportExpiryDate());

        return profileDto;
    }

}