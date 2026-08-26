package com.rioni.lk.api.repository;

import com.rioni.lk.api.model.Tariff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TariffRepository extends JpaRepository<Tariff, Integer> {
}
