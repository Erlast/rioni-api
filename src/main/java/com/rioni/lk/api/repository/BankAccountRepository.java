package com.rioni.lk.api.repository;

import com.rioni.lk.api.model.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {
    List<BankAccount> findByProfileId(int profileId);
}