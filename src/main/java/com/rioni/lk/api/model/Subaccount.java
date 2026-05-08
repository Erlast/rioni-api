package com.rioni.lk.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "subaccount")
public class Subaccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "subaccount_id")
    private int id;

    @Column(name = "account_id")
    private int accountId;

     @Column(name = "subaccount_number")
    private String accountNumber;

     @Column(name = "subaccount_type_code")
    private String subaccountTypeCode;

    @OneToOne(fetch = FetchType.LAZY) 
    @JoinColumn(name = "account_id", insertable = false, updatable = false)
    private Account account;
}
