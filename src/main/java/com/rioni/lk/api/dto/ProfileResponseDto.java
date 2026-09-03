package com.rioni.lk.api.dto;

import lombok.Data;

import java.sql.Timestamp;
import java.util.List;
import java.math.BigDecimal;

@Data
public class ProfileResponseDto {
    private int id;
    private BigDecimal balance;
    private String nbs;
    private String ndu;
    private String name;
    private String surname;
    private String nickname;
    private String login;
    private String patronymic;
    private String photoUrl;
    private String dateOfBirth;
    private String gender;
    private String citizenship;
    private String placeOfBirth;
    private String documentType;
    private String passportNumber;
    private String passportIssueDate;
    private String passportExpiryDate;
    private String companyName;
    private String companyIndustry;
    private String companyPosition;
    private String companyPhone;
    private String companyWebsite;
    private Boolean isNpo;
    private Boolean isNgo;
    private Boolean isSelfEmployed;
    private Boolean isNotWorking;
    private String issuedBy;
    private List<ProfileContactDto> contacts;
    private List<ProfileAddressDto> addresses;
    private Boolean hasBeneficiaries;
    private Boolean isPep;
    private Boolean noResidencePermit;
    private Integer tariffId;
    private Timestamp tariffStartDate;
}