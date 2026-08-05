package com.rioni.lk.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "sms_codes")
public class SmsCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "phone")
    private String phone;

    @Column(name = "code")
    private String code;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "is_used")
    private boolean isUsed;

    @Column(name = "attempted_count")
    private int attemptedCount;

    @Column(name = "purpose")
    private String purpose;
}