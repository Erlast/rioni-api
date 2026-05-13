package com.rioni.lk.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "profile")
public class Profile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;
    private String Name;
    private String Nbs;
    private String Ndu;
    private String Surname;
    private String Nickname;
    private String Login;
    private String Patronymic;
    @Column(columnDefinition = "text")
    private String PhotoUrl;
    private String Gender;
    private String Citizenship;
    private String DateOfBirth;
    private String PlaceOfBirth;
    private String DocumentType;
    private String PassportNumber;
    private String PassportIssueDate;
    private String PassportExpiryDate;
    private String CompanyName;
    private String CompanyIndustry;
    private String CompanyPosition;
    private String CompanyPhone;
    private String CompanyWebsite;
    private Boolean isNpo;
    private Boolean isNgo;
    private Boolean isSelfEmployed;
    private Boolean isNotWorking;
    private String issuedBy;

}