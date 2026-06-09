package com.rioni.lk.api.repository;

import com.rioni.lk.api.model.ProfileContact;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProfileContactRepository extends JpaRepository<ProfileContact, Long> {
    List<ProfileContact> findByProfileId(int profileId);
    Optional<ProfileContact> findTopByProfileIdAndContactTypeOrderByIsMainDesc(int profileId, String contactType);
    Optional<ProfileContact> findTopByValueAndContactTypeOrderByIsMainDesc(String value, String contactType);
}