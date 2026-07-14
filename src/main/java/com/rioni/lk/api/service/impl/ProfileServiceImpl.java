package com.rioni.lk.api.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import com.rioni.lk.api.dto.ProfileDto;
import com.rioni.lk.api.dto.ProfileResponseDto;
import com.rioni.lk.api.dto.ProfileContactDto;
import com.rioni.lk.api.dto.ProfileAddressDto;
import com.rioni.lk.api.dto.TaxResidenceDto;
import com.rioni.lk.api.dto.ResidencePermitDto;
import com.rioni.lk.api.dto.BankAccountDto;
import com.rioni.lk.api.service.ProfileService;
import com.rioni.lk.api.service.FileUploadService;
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
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

@Service
public class ProfileServiceImpl implements ProfileService {
    private static final Logger log = LoggerFactory.getLogger(ProfileServiceImpl.class);

    private final ProfileRepository profileRepository;
    private final ProfileContactRepository profileContactRepository;
    private final ProfileAddressRepository profileAddressRepository;
    private final TaxResidenceRepository taxResidenceRepository;
    private final ResidencePermitRepository residencePermitRepository;
    private final BankAccountRepository bankAccountRepository;
    private final FileUploadService fileUploadService;

    @Value("${app.uploads.base-url}")
    private String uploadsBaseUrl;

    @Autowired
    public ProfileServiceImpl(ProfileRepository profileRepository, ProfileContactRepository profileContactRepository, ProfileAddressRepository profileAddressRepository, TaxResidenceRepository taxResidenceRepository, ResidencePermitRepository residencePermitRepository, BankAccountRepository bankAccountRepository, FileUploadService fileUploadService) {
        this.profileRepository = profileRepository;
        this.profileContactRepository = profileContactRepository;
        this.profileAddressRepository = profileAddressRepository;
        this.taxResidenceRepository = taxResidenceRepository;
        this.residencePermitRepository = residencePermitRepository;
        this.bankAccountRepository = bankAccountRepository;
        this.fileUploadService = fileUploadService;
    }

    @Override
    public ProfileResponseDto getProfileById(Long id) {
        return profileRepository.findById(id)
                .map(profile -> {
                    List<ProfileContact> contacts = profileContactRepository.findByProfileId(profile.getId());
                    List<ProfileAddress> addresses = profileAddressRepository.findByProfileId(profile.getId());
                    ProfileResponseDto dto = ProfileMapper.mapToDto(profile, contacts, addresses);
                    // Construct full avatar URL if filename is stored
                    if (dto.getPhotoUrl() != null && !dto.getPhotoUrl().isEmpty()) {
                        String avatarUrl = uploadsBaseUrl + profile.getId() + "/avatar/" + dto.getPhotoUrl();
                        dto.setPhotoUrl(avatarUrl);
                    }
                    return dto;
                })
                .orElse(null);
    }

    @Override
    public ProfileResponseDto saveProfile(Long id, ProfileDto profileDto) {
        return profileRepository.findById(id)
                .map(profile -> {
                    ProfileMapper.mapToEntity(profileDto, profile);
                    ProfileResponseDto dto = ProfileMapper.mapToDto(profileRepository.save(profile), List.of(), List.of());
                    // Construct full avatar URL if filename is stored
                    if (dto.getPhotoUrl() != null && !dto.getPhotoUrl().isEmpty()) {
                        String avatarUrl = uploadsBaseUrl + profile.getId() + "/avatar/" + dto.getPhotoUrl();
                        dto.setPhotoUrl(avatarUrl);
                    }
                    return dto;
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
    public boolean saveAvatar(Long profileId, MultipartFile file) {
        return profileRepository.findById(profileId)
                .map(profile -> {
                    // Delete the previous avatar file if it exists
                    String oldPhotoUrl = profile.getPhotoUrl();
                    if (oldPhotoUrl != null && !oldPhotoUrl.isEmpty()) {
                        log.info("Deleting previous avatar for profileId={}: {}", profileId, oldPhotoUrl);
                        fileUploadService.deleteFile(profileId, oldPhotoUrl, "avatar");
                    }

                    // Upload new file using FileUploadService -> saves to uploads/<profileId>/avatar/<uuid>.ext
                    String fileUrl = fileUploadService.uploadFile(profileId, file, "avatar");
                    // Extract just the filename from the URL (last segment after /)
                    String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
                    // Save only the filename in the database
                    profile.setPhotoUrl(fileName);
                    profileRepository.save(profile);
                    return true;
                })
                .orElse(false);
    }

    @Override
    @Transactional
    public boolean deleteAvatar(Long profileId) {
        return profileRepository.findById(profileId)
                .map(profile -> {
                    String photoUrl = profile.getPhotoUrl();
                    if (photoUrl == null || photoUrl.isEmpty()) {
                        log.warn("deleteAvatar called for profileId={} but photoUrl is already null/empty", profileId);
                        return false;
                    }
                    // Delete the physical file from storage
                    fileUploadService.deleteFile(profileId, photoUrl, "avatar");
                    // Set photoUrl to null in the database
                    profile.setPhotoUrl(null);
                    profileRepository.save(profile);
                    return true;
                })
                .orElse(false);
    }
}