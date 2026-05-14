package com.rioni.lk.api.repository;

import com.rioni.lk.api.model.ResidencePermit;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ResidencePermitRepository extends JpaRepository<ResidencePermit, Long> {
    List<ResidencePermit> findByProfileId(int profileId);
}