package com.rioni.lk.api.repository;

import com.rioni.lk.api.model.ProfileAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProfileAddressRepository extends JpaRepository<ProfileAddress, Long> {
    List<ProfileAddress> findByProfileId(int profileId);
}