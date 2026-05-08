package com.rioni.lk.api.mapper;

import lombok.NoArgsConstructor;
import com.rioni.lk.api.dto.ProfileDto;
import com.rioni.lk.api.dto.ProfileResponseDto;
import com.rioni.lk.api.dto.Nullable;
import com.rioni.lk.api.model.Profile;

@NoArgsConstructor
public class ProfileMapper {
    public static ProfileResponseDto mapToDto(Profile profile) {
        ProfileResponseDto dto = new ProfileResponseDto();
        dto.setId(profile.getId());
        dto.setNbs(profile.getNbs());
        dto.setNdu(profile.getNdu());
        dto.setName(profile.getName());
        dto.setSurname(profile.getSurname());
        dto.setPatronymic(profile.getPatronymic());
        dto.setPhotoUrl(profile.getPhotoUrl());
        dto.setEmail(profile.getEmail());
        dto.setPhone(profile.getPhone());
        dto.setDateOfBirth(profile.getDateOfBirth());
        dto.setGender(profile.getGender());
        dto.setCitizenship(profile.getCitizenship());
        dto.setPlaceOfBirth(profile.getPlaceOfBirth());
        dto.setCountryOfBirth(profile.getCountryOfBirth());
        dto.setCityOfBirth(profile.getCityOfBirth());
        dto.setDocumentType(profile.getDocumentType());
        dto.setPassportNumber(profile.getPassportNumber());
        dto.setPassportIssueDate(profile.getPassportIssueDate());
        dto.setPassportExpiryDate(profile.getPassportExpiryDate());
        dto.setIssuedBy(profile.getIssuedBy());
        dto.setCompanyName(profile.getCompanyName());
        dto.setCompanyIndustry(profile.getCompanyIndustry());
        dto.setCompanyPosition(profile.getCompanyPosition());
        dto.setCompanyPhone(profile.getCompanyPhone());
        dto.setCompanyWebsite(profile.getCompanyWebsite());
        dto.setIsNpo(profile.getIsNpo());
        dto.setIsNgo(profile.getIsNgo());
        dto.setIsSelfEmployed(profile.getIsSelfEmployed());
        dto.setIsNotWorking(profile.getIsNotWorking());
        return dto;
    }

    public static void mapToEntity(ProfileDto dto, Profile profile) {
        if (dto.getNbs() != null && dto.getNbs().wasSent()) profile.setNbs((String) dto.getNbs().getValue());
        if (dto.getNdu() != null && dto.getNdu().wasSent()) profile.setNdu((String) dto.getNdu().getValue());
        if (dto.getName() != null && dto.getName().wasSent()) profile.setName((String) dto.getName().getValue());
        if (dto.getSurname() != null && dto.getSurname().wasSent()) profile.setSurname((String) dto.getSurname().getValue());
        if (dto.getPatronymic() != null && dto.getPatronymic().wasSent()) profile.setPatronymic((String) dto.getPatronymic().getValue());
        if (dto.getPhotoUrl() != null && dto.getPhotoUrl().wasSent()) profile.setPhotoUrl((String) dto.getPhotoUrl().getValue());
        if (dto.getEmail() != null && dto.getEmail().wasSent()) profile.setEmail((String) dto.getEmail().getValue());
        if (dto.getPhone() != null && dto.getPhone().wasSent()) profile.setPhone((String) dto.getPhone().getValue());
        if (dto.getDateOfBirth() != null && dto.getDateOfBirth().wasSent()) profile.setDateOfBirth((String) dto.getDateOfBirth().getValue());
        if (dto.getGender() != null && dto.getGender().wasSent()) profile.setGender((String) dto.getGender().getValue());
        if (dto.getCitizenship() != null && dto.getCitizenship().wasSent()) profile.setCitizenship((String) dto.getCitizenship().getValue());
        if (dto.getPlaceOfBirth() != null && dto.getPlaceOfBirth().wasSent()) profile.setPlaceOfBirth((String) dto.getPlaceOfBirth().getValue());
        if (dto.getCountryOfBirth() != null && dto.getCountryOfBirth().wasSent()) profile.setCountryOfBirth((String) dto.getCountryOfBirth().getValue());
        if (dto.getCityOfBirth() != null && dto.getCityOfBirth().wasSent()) profile.setCityOfBirth((String) dto.getCityOfBirth().getValue());
        if (dto.getDocumentType() != null && dto.getDocumentType().wasSent()) profile.setDocumentType((String) dto.getDocumentType().getValue());
        if (dto.getPassportNumber() != null && dto.getPassportNumber().wasSent()) profile.setPassportNumber((String) dto.getPassportNumber().getValue());
        if (dto.getPassportIssueDate() != null && dto.getPassportIssueDate().wasSent()) profile.setPassportIssueDate((String) dto.getPassportIssueDate().getValue());
        if (dto.getPassportExpiryDate() != null && dto.getPassportExpiryDate().wasSent()) profile.setPassportExpiryDate((String) dto.getPassportExpiryDate().getValue());
        if (dto.getIssuedBy() != null && dto.getIssuedBy().wasSent()) profile.setIssuedBy((String) dto.getIssuedBy().getValue());
        if (dto.getCompanyName() != null && dto.getCompanyName().wasSent()) profile.setCompanyName((String) dto.getCompanyName().getValue());
        if (dto.getCompanyIndustry() != null && dto.getCompanyIndustry().wasSent()) profile.setCompanyIndustry((String) dto.getCompanyIndustry().getValue());
        if (dto.getCompanyPosition() != null && dto.getCompanyPosition().wasSent()) profile.setCompanyPosition((String) dto.getCompanyPosition().getValue());
        if (dto.getCompanyPhone() != null && dto.getCompanyPhone().wasSent()) profile.setCompanyPhone((String) dto.getCompanyPhone().getValue());
        if (dto.getCompanyWebsite() != null && dto.getCompanyWebsite().wasSent()) profile.setCompanyWebsite((String) dto.getCompanyWebsite().getValue());
        if (dto.getIsNpo() != null && dto.getIsNpo().wasSent()) profile.setIsNpo((Boolean) dto.getIsNpo().getValue());
        if (dto.getIsNgo() != null && dto.getIsNgo().wasSent()) profile.setIsNgo((Boolean) dto.getIsNgo().getValue());
        if (dto.getIsSelfEmployed() != null && dto.getIsSelfEmployed().wasSent()) profile.setIsSelfEmployed((Boolean) dto.getIsSelfEmployed().getValue());
        if (dto.getIsNotWorking() != null && dto.getIsNotWorking().wasSent()) profile.setIsNotWorking((Boolean) dto.getIsNotWorking().getValue());
    }

}