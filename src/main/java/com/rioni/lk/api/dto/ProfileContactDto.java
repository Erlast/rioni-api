package com.rioni.lk.api.dto;

import lombok.Data;

@Data
public class ProfileContactDto {
    private int id;
    private int profileId;
    private String contactType;
    private Boolean isMain;
    private String value;
    private Boolean isConfirmed;
}