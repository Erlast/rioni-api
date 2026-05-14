package com.rioni.lk.api.repository;

import com.rioni.lk.api.model.TaxResidence;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TaxResidenceRepository extends JpaRepository<TaxResidence, Long> {
    List<TaxResidence> findByProfileId(int profileId);
}