package com.rioni.lk.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class ProfileDto {
    @JsonIgnore
    private int id;

    private Nullable<String> nbs;
    private Nullable<String> ndu;
    private Nullable<String> name;
    private Nullable<String> surname;
    private Nullable<String> patronymic;
    private Nullable<String> photoUrl;
    private Nullable<String> email;
    private Nullable<String> phone;
    private Nullable<String> dateOfBirth;
    private Nullable<String> gender;
    private Nullable<String> citizenship;
    private Nullable<String> placeOfBirth;
    private Nullable<String> countryOfBirth;
    private Nullable<String> cityOfBirth;
    private Nullable<String> documentType;
    private Nullable<String> passportNumber;
    private Nullable<String> passportIssueDate;
    private Nullable<String> passportExpiryDate;
    private Nullable<String> companyName;
    private Nullable<String> companyIndustry;
    private Nullable<String> companyPosition;
    private Nullable<String> companyPhone;
    private Nullable<String> companyWebsite;
    private Nullable<Boolean> isNgo;
    private Nullable<Boolean> isNotWorking;
    private Nullable<Boolean> isNpo;
    private Nullable<Boolean> isSelfEmployed;
    private Nullable<String> issuedBy;
    private Nullable<String> nickname;
    private Nullable<String> login;
    private Nullable<Boolean> hasBeneficiaries;
    private Nullable<Boolean> isPep;
    private Nullable<Boolean> noResidencePermit;

    public ProfileDto() {
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Nullable<String> getNbs() { return nbs; }
    public void setNbs(String v) { this.nbs = new Nullable<>(v, true); }

    public Nullable<String> getNdu() { return ndu; }
    public void setNdu(String v) { this.ndu = new Nullable<>(v, true); }

    public Nullable<String> getName() { return name; }
    public void setName(String v) { this.name = new Nullable<>(v, true); }

    public Nullable<String> getSurname() { return surname; }
    public void setSurname(String v) { this.surname = new Nullable<>(v, true); }

    public Nullable<String> getPatronymic() { return patronymic; }
    public void setPatronymic(String v) { this.patronymic = new Nullable<>(v, true); }

    public Nullable<String> getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String v) { this.photoUrl = new Nullable<>(v, true); }

    public Nullable<String> getEmail() { return email; }
    public void setEmail(String v) { this.email = new Nullable<>(v, true); }

    public Nullable<String> getPhone() { return phone; }
    public void setPhone(String v) { this.phone = new Nullable<>(v, true); }

    public Nullable<String> getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(String v) { this.dateOfBirth = new Nullable<>(v, true); }

    public Nullable<String> getGender() { return gender; }
    public void setGender(String v) { this.gender = new Nullable<>(v, true); }

    public Nullable<String> getCitizenship() { return citizenship; }
    public void setCitizenship(String v) { this.citizenship = new Nullable<>(v, true); }

    public Nullable<String> getPlaceOfBirth() { return placeOfBirth; }
    public void setPlaceOfBirth(String v) { this.placeOfBirth = new Nullable<>(v, true); }

    public Nullable<String> getCountryOfBirth() { return countryOfBirth; }
    public void setCountryOfBirth(String v) { this.countryOfBirth = new Nullable<>(v, true); }

    public Nullable<String> getCityOfBirth() { return cityOfBirth; }
    public void setCityOfBirth(String v) { this.cityOfBirth = new Nullable<>(v, true); }

    public Nullable<String> getDocumentType() { return documentType; }
    public void setDocumentType(String v) { this.documentType = new Nullable<>(v, true); }

    public Nullable<String> getPassportNumber() { return passportNumber; }
    public void setPassportNumber(String v) { this.passportNumber = new Nullable<>(v, true); }

    public Nullable<String> getPassportIssueDate() { return passportIssueDate; }
    public void setPassportIssueDate(String v) { this.passportIssueDate = new Nullable<>(v, true); }

    public Nullable<String> getPassportExpiryDate() { return passportExpiryDate; }
    public void setPassportExpiryDate(String v) { this.passportExpiryDate = new Nullable<>(v, true); }

    public Nullable<String> getCompanyName() { return companyName; }
    public void setCompanyName(String v) { this.companyName = new Nullable<>(v, true); }

    public Nullable<String> getCompanyIndustry() { return companyIndustry; }
    public void setCompanyIndustry(String v) { this.companyIndustry = new Nullable<>(v, true); }

    public Nullable<String> getCompanyPosition() { return companyPosition; }
    public void setCompanyPosition(String v) { this.companyPosition = new Nullable<>(v, true); }

    public Nullable<String> getCompanyPhone() { return companyPhone; }
    public void setCompanyPhone(String v) { this.companyPhone = new Nullable<>(v, true); }

    public Nullable<String> getCompanyWebsite() { return companyWebsite; }
    public void setCompanyWebsite(String v) { this.companyWebsite = new Nullable<>(v, true); }

    public Nullable<Boolean> getIsNgo() { return isNgo; }
    public void setIsNgo(Boolean v) { this.isNgo = new Nullable<>(v, true); }

    public Nullable<Boolean> getIsNotWorking() { return isNotWorking; }
    public void setIsNotWorking(Boolean v) { this.isNotWorking = new Nullable<>(v, true); }

    public Nullable<Boolean> getIsNpo() { return isNpo; }
    public void setIsNpo(Boolean v) { this.isNpo = new Nullable<>(v, true); }

    public Nullable<Boolean> getIsSelfEmployed() { return isSelfEmployed; }
    public void setIsSelfEmployed(Boolean v) { this.isSelfEmployed = new Nullable<>(v, true); }

    public Nullable<String> getIssuedBy() { return issuedBy; }
    public void setIssuedBy(String v) { this.issuedBy = new Nullable<>(v, true); }

    public Nullable<String> getNickname() { return nickname; }
    public void setNickname(String v) { this.nickname = new Nullable<>(v, true); }

    public Nullable<String> getLogin() { return login; }
    public void setLogin(String v) { this.login = new Nullable<>(v, true); }

    public Nullable<Boolean> getHasBeneficiaries() { return hasBeneficiaries; }
    public void setHasBeneficiaries(Boolean v) { this.hasBeneficiaries = new Nullable<>(v, true); }

    public Nullable<Boolean> getIsPep() { return isPep; }
    public void setIsPep(Boolean v) { this.isPep = new Nullable<>(v, true); }

    public Nullable<Boolean> getNoResidencePermit() { return noResidencePermit; }
    public void setNoResidencePermit(Boolean v) { this.noResidencePermit = new Nullable<>(v, true); }
}