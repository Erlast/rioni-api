package com.rioni.lk.api.dto;

import lombok.Data;

@Data
public class ResidencePermitDto {
    private int id;
    private String country;
    private String issuedBy;
    private String documentNumber;
    private String stayPeriod;
}