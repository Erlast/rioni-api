package com.rioni.lk.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "profile_contacts")
public class ProfileContact {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "profile_id")
    private int profileId;

    @Column(name = "contact_type")
    private String contactType;

    @Column(name = "is_main")
    private Boolean isMain;

    @Column(name = "value")
    private String value;

    @Column(name = "is_confirmed")
    private Boolean isConfirmed;
}