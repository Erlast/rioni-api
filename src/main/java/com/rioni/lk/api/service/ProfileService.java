package com.rioni.lk.api.service;

import com.rioni.lk.api.dto.ProfileDto;
import com.rioni.lk.api.dto.ProfileResponseDto;
import com.rioni.lk.api.dto.ProfileContactDto;
import com.rioni.lk.api.dto.ProfileAddressDto;
import com.rioni.lk.api.dto.TaxResidenceDto;
import com.rioni.lk.api.dto.ResidencePermitDto;
import com.rioni.lk.api.dto.BankAccountDto;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface ProfileService {

    ProfileResponseDto getProfileById(Long id);

    ProfileResponseDto saveProfile(Long id, ProfileDto profileDto);

    boolean saveContacts(Long profileId, List<ProfileContactDto> contacts);

    boolean saveAddresses(Long profileId, List<ProfileAddressDto> addresses);

    List<TaxResidenceDto> getTaxResidences(Long profileId);

    List<ResidencePermitDto> getResidencePermits(Long profileId);

    boolean saveTaxResidences(Long profileId, List<TaxResidenceDto> taxResidences);

    boolean saveResidencePermits(Long profileId, List<ResidencePermitDto> residencePermits);

    List<BankAccountDto> getBankAccounts(Long profileId);

    boolean saveBankAccounts(Long profileId, List<BankAccountDto> bankAccounts);

    boolean saveAvatar(Long profileId, MultipartFile file);

    boolean deleteAvatar(Long profileId);

    boolean saveTariff(Long profileId, Integer tariffId);

}