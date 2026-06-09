package com.rioni.lk.api.repository;

import com.rioni.lk.api.model.SmsCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SmsCodeRepository extends JpaRepository<SmsCode, Long> {
    Optional<SmsCode> findTopByPhoneOrderByCreatedAtDesc(String phone);
}