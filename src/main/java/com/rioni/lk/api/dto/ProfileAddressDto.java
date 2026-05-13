package com.rioni.lk.api.dto;

import lombok.Data;

@Data
public class ProfileAddressDto {
    private int id;
    private int profileId;
    private String country;
    private String city;
    private String postcode;
    private String address;
    private Boolean isMain;
    private Boolean isConfirmed;
    private String addressType;
}