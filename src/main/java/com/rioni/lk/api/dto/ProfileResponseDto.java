package com.rioni.lk.api.dto;

import lombok.Data;

@Data
public class ProfileResponseDto {
    private int id;
    private String nbs;
    private String ndu;
    private String name;
    private String surname;
    private String patronymic;
    private String photoUrl;
    private String email;
    private String phone;
    private String dateOfBirth;
    private String gender;
    private String citizenship;
    private String placeOfBirth;
    private String countryOfBirth;
    private String cityOfBirth;
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
}