package com.rioni.lk.api.repository;

import com.rioni.lk.api.model.ProfilePassword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProfilePasswordRepository extends JpaRepository<ProfilePassword, Integer> {
    
    @Query("SELECT pp FROM ProfilePassword pp JOIN FETCH pp.profile p WHERE p.Login = :login")
    Optional<ProfilePassword> findByProfileLogin(@Param("login") String login);

    @Query("SELECT pp FROM ProfilePassword pp JOIN FETCH pp.profile p WHERE p.id = :profileId")
    Optional<ProfilePassword> findByProfileId(@Param("profileId") int profileId);
}
