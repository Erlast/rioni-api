package com.rioni.lk.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
public class ProfileDto {
    @JsonIgnore
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

    // Конструктор по умолчанию
    public ProfileDto() {
    }

    // Конструктор со всеми полями (опционально)
    public ProfileDto(
            int id, String nbs, String ndu, String name, String surname,
            String patronymic, String photoUrl, String email, String phone, String dateOfBirth,
            String gender, String citizenship, String placeOfBirth, String countryOfBirth, String cityOfBirth,
            String documentType, String passportNumber, String passportIssueDate,
            String passportExpiryDate) {
        this.id = id;
        this.nbs = nbs;
        this.ndu = ndu;
        this.name = name;
        this.surname = surname;
        this.patronymic = patronymic;
        this.photoUrl = photoUrl;
        this.email = email;
        this.phone = phone;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.citizenship = citizenship;
        this.placeOfBirth = placeOfBirth;
        this.countryOfBirth = countryOfBirth;
        this.cityOfBirth = cityOfBirth;
        this.documentType = documentType;
        this.passportNumber = passportNumber;
        this.passportIssueDate = passportIssueDate;
        this.passportExpiryDate = passportExpiryDate;
    }

    // Геттеры и сеттеры
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNbs() {
        return nbs;
    }

    public void setNbs(String nbs) {
        this.nbs = nbs;
    }

    public String getNdu() {
        return ndu;
    }

    public void setNdu(String ndu) {
        this.ndu = ndu;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getPatronymic() {
        return patronymic;
    }

    public void setPatronymic(String patronymic) {
        this.patronymic = patronymic;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getCitizenship() {
        return citizenship;
    }

    public void setCitizenship(String citizenship) {
        this.citizenship = citizenship;
    }

    public String getPlaceOfBirth() {
        return placeOfBirth;
    }

    public void setPlaceOfBirth(String placeOfBirth) {
        this.placeOfBirth = placeOfBirth;
    }

    public String getCountryOfBirth() {
        return countryOfBirth;
    }

    public void setCountryOfBirth(String countryOfBirth) {
        this.countryOfBirth = countryOfBirth;
    }

    public String getCityOfBirth() {
        return cityOfBirth;
    }

    public void setCityOfBirth(String cityOfBirth) {
        this.cityOfBirth = cityOfBirth;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public String getPassportNumber() {
        return passportNumber;
    }

    public void setPassportNumber(String passportNumber) {
        this.passportNumber = passportNumber;
    }

    public String getPassportIssueDate() {
        return passportIssueDate;
    }

    public void setPassportIssueDate(String passportIssueDate) {
        this.passportIssueDate = passportIssueDate;
    }

    public String getPassportExpiryDate() {
        return passportExpiryDate;
    }

    public void setPassportExpiryDate(String passportExpiryDate) {
        this.passportExpiryDate = passportExpiryDate;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getCompanyIndustry() {
        return companyIndustry;
    }

    public void setCompanyIndustry(String companyIndustry) {
        this.companyIndustry = companyIndustry;
    }

    public String getCompanyPosition() {
        return companyPosition;
    }

    public void setCompanyPosition(String companyPosition) {
        this.companyPosition = companyPosition;
    }

    public String getCompanyPhone() {
        return companyPhone;
    }

    public void setCompanyPhone(String companyPhone) {
        this.companyPhone = companyPhone;
    }

    public String getCompanyWebsite() {
        return companyWebsite;
    }

    public void setCompanyWebsite(String companyWebsite) {
        this.companyWebsite = companyWebsite;
    }

    public Boolean getIsNpo() {
        return isNpo;
    }

    public void setIsNpo(Boolean isNpo) {
        this.isNpo = isNpo;
    }

    public Boolean getIsNgo() {
        return isNgo;
    }

    public void setIsNgo(Boolean isNgo) {
        this.isNgo = isNgo;
    }

    public Boolean getIsSelfEmployed() {
        return isSelfEmployed;
    }

    public void setIsSelfEmployed(Boolean isSelfEmployed) {
        this.isSelfEmployed = isSelfEmployed;
    }

    public Boolean getIsNotWorking() {
        return isNotWorking;
    }

    public void setIsNotWorking(Boolean isNotWorking) {
        this.isNotWorking = isNotWorking;
    }
}