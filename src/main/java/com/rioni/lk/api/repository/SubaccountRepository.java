package com.rioni.lk.api.repository;

import com.rioni.lk.api.model.Subaccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SubaccountRepository extends JpaRepository<Subaccount, Integer> {
    List<Subaccount> findByAccountId(Integer accountId);
}