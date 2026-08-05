package com.rioni.lk.api.repository;

import com.rioni.lk.api.model.GlossaryEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GlossaryEntryRepository extends JpaRepository<GlossaryEntry, Integer> {

    List<GlossaryEntry> findByLanguageOrderByTermAsc(String language);

    List<GlossaryEntry> findByLanguageAndLetterOrderByTermAsc(String language, String letter);
}
