package com.rioni.lk.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "residence_permits")
public class ResidencePermit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "profile_id")
    private int profileId;

    @Column(name = "country")
    private String country;

    @Column(name = "issued_by")
    private String issuedBy;

    @Column(name = "document_number")
    private String documentNumber;

    @Column(name = "stay_period")
    private String stayPeriod;
}