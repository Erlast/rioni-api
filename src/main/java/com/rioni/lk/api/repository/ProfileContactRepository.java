package com.rioni.lk.api.repository;

import com.rioni.lk.api.model.ProfileContact;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProfileContactRepository extends JpaRepository<ProfileContact, Long> {
    List<ProfileContact> findByProfileId(int profileId);
}