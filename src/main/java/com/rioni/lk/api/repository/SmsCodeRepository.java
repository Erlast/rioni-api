package com.rioni.lk.api.repository;

import com.rioni.lk.api.model.SmsCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface SmsCodeRepository extends JpaRepository<SmsCode, Long> {
    Optional<SmsCode> findTopByPhoneOrderByCreatedAtDesc(String phone);

    @Modifying
    @Query("UPDATE SmsCode s SET s.attemptedCount = s.attemptedCount + 1 WHERE s.id = :id")
    int incrementAttemptedCount(long id);
}