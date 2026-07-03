package com.rioni.lk.api.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import com.rioni.lk.api.dto.ProfileDto;
import com.rioni.lk.api.dto.ProfileResponseDto;
import com.rioni.lk.api.dto.ProfileContactDto;
import com.rioni.lk.api.dto.ProfileAddressDto;
import com.rioni.lk.api.dto.TaxResidenceDto;
import com.rioni.lk.api.dto.ResidencePermitDto;
import com.rioni.lk.api.dto.BankAccountDto;
import com.rioni.lk.api.service.ProfileService;
import com.rioni.lk.api.repository.ProfileRepository;
import com.rioni.lk.api.repository.ProfileContactRepository;
import com.rioni.lk.api.repository.ProfileAddressRepository;
import com.rioni.lk.api.repository.TaxResidenceRepository;
import com.rioni.lk.api.repository.ResidencePermitRepository;
import com.rioni.lk.api.repository.BankAccountRepository;
import com.rioni.lk.api.mapper.ProfileMapper;
import com.rioni.lk.api.model.ProfileContact;
import com.rioni.lk.api.model.ProfileAddress;
import com.rioni.lk.api.model.TaxResidence;
import com.rioni.lk.api.model.ResidencePermit;
import com.rioni.lk.api.model.BankAccount;
import com.rioni.lk.api.util.PhoneUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class ProfileServiceImpl implements ProfileService {
    private final ProfileRepository profileRepository;
    private final ProfileContactRepository profileContactRepository;
    private final ProfileAddressRepository profileAddressRepository;
    private final TaxResidenceRepository taxResidenceRepository;
    private final ResidencePermitRepository residencePermitRepository;
    private final BankAccountRepository bankAccountRepository;

    @Autowired
    public ProfileServiceImpl(ProfileRepository profileRepository, ProfileContactRepository profileContactRepository, ProfileAddressRepository profileAddressRepository, TaxResidenceRepository taxResidenceRepository, ResidencePermitRepository residencePermitRepository, BankAccountRepository bankAccountRepository) {
        this.profileRepository = profileRepository;
        this.profileContactRepository = profileContactRepository;
        this.profileAddressRepository = profileAddressRepository;
        this.taxResidenceRepository = taxResidenceRepository;
        this.residencePermitRepository = residencePermitRepository;
        this.bankAccountRepository = bankAccountRepository;
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
            String value = contactDto.getValue();
            if ("phone".equals(contactDto.getContactType())) {
                value = PhoneUtils.normalize(value);
            }
            contact.setValue(value);
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

    @Override
    public List<TaxResidenceDto> getTaxResidences(Long profileId) {
        return taxResidenceRepository.findByProfileId(profileId.intValue())
                .stream()
                .map(tr -> {
                    TaxResidenceDto dto = new TaxResidenceDto();
                    dto.setId(tr.getId());
                    dto.setCountry(tr.getCountry());
                    dto.setInn(tr.getInn());
                    return dto;
                })
                .toList();
    }

    @Override
    public List<ResidencePermitDto> getResidencePermits(Long profileId) {
        return residencePermitRepository.findByProfileId(profileId.intValue())
                .stream()
                .map(rp -> {
                    ResidencePermitDto dto = new ResidencePermitDto();
                    dto.setId(rp.getId());
                    dto.setCountry(rp.getCountry());
                    dto.setIssuedBy(rp.getIssuedBy());
                    dto.setDocumentNumber(rp.getDocumentNumber());
                    dto.setStayPeriod(rp.getStayPeriod());
                    return dto;
                })
                .toList();
    }

    @Override
    @Transactional
    public boolean saveTaxResidences(Long profileId, List<TaxResidenceDto> taxResidences) {
        if (!profileRepository.existsById(profileId)) {
            return false;
        }
        for (TaxResidenceDto dto : taxResidences) {
            TaxResidence residence = new TaxResidence();
            residence.setProfileId(profileId.intValue());
            residence.setCountry(dto.getCountry());
            residence.setInn(dto.getInn());
            if (dto.getId() != 0) {
                residence.setId(dto.getId());
            }
            taxResidenceRepository.save(residence);
        }
        return true;
    }

    @Override
    @Transactional
    public boolean saveResidencePermits(Long profileId, List<ResidencePermitDto> residencePermits) {
        if (!profileRepository.existsById(profileId)) {
            return false;
        }
        for (ResidencePermitDto dto : residencePermits) {
            ResidencePermit permit = new ResidencePermit();
            permit.setProfileId(profileId.intValue());
            permit.setCountry(dto.getCountry());
            permit.setIssuedBy(dto.getIssuedBy());
            permit.setDocumentNumber(dto.getDocumentNumber());
            permit.setStayPeriod(dto.getStayPeriod());
            if (dto.getId() != 0) {
                permit.setId(dto.getId());
            }
            residencePermitRepository.save(permit);
        }
        return true;
    }

    @Override
    public List<BankAccountDto> getBankAccounts(Long profileId) {
        return bankAccountRepository.findByProfileId(profileId.intValue())
                .stream()
                .map(ba -> {
                    BankAccountDto dto = new BankAccountDto();
                    dto.setId(ba.getId());
                    dto.setCountry(ba.getCountry());
                    dto.setBankName(ba.getBankName());
                    dto.setIban(ba.getIban());
                    dto.setSwift(ba.getSwift());
                    dto.setIsMain(ba.getIsMain());
                    dto.setIsConfirmed(ba.getIsConfirmed());
                    dto.setIsBlocked(ba.getIsBlocked());
                    return dto;
                })
                .toList();
    }

    @Override
    @Transactional
    public boolean saveBankAccounts(Long profileId, List<BankAccountDto> bankAccounts) {
        if (!profileRepository.existsById(profileId)) {
            return false;
        }
        for (BankAccountDto dto : bankAccounts) {
            BankAccount bankAccount = new BankAccount();
            bankAccount.setProfileId(profileId.intValue());
            bankAccount.setCountry(dto.getCountry());
            bankAccount.setBankName(dto.getBankName());
            bankAccount.setIban(dto.getIban());
            bankAccount.setSwift(dto.getSwift());
            bankAccount.setIsMain(dto.getIsMain());
            bankAccount.setIsConfirmed(dto.getIsConfirmed());
            bankAccount.setIsBlocked(dto.getIsBlocked());
            if (dto.getId() != 0) {
                bankAccount.setId(dto.getId());
            }
            bankAccountRepository.save(bankAccount);
        }
        return true;
    }

    @Override
    @Transactional
    public boolean saveAvatar(Long profileId, String base64Image) {
        return profileRepository.findById(profileId)
                .map(profile -> {
                    profile.setPhotoUrl(base64Image);
                    profileRepository.save(profile);
                    return true;
                })
                .orElse(false);
    }
}