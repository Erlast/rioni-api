package com.rioni.lk.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "account")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "profile_id")
    private int profileId;

     @Column(name = "account_number")
    private String accountNumber;

     @Column(name = "account_type")
    private String accountType;

     @Column(name = "account_currency_id")
    private int accountCurrencyId;
}
