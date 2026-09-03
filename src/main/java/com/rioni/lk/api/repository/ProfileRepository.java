package com.rioni.lk.api.repository;

import com.rioni.lk.api.model.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProfileRepository extends JpaRepository<Profile, Long> {
    Optional<Profile> findProfilekById(Long id);

    @Query("SELECT p FROM Profile p WHERE p.Login = :login")
    Optional<Profile> findByLogin(@Param("login") String login);
}
