package com.rioni.lk.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.rioni.lk.api.model.Profile;
import java.util.Optional;

public interface ProfileRepository extends JpaRepository<Profile, Long> {
    Optional<Profile> findProfilekById(Long id);
}
