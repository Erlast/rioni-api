package com.rioni.lk.api.service.impl;

import com.rioni.lk.api.dto.GlossaryEntryDto;
import com.rioni.lk.api.repository.GlossaryEntryRepository;
import com.rioni.lk.api.service.GlossaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GlossaryServiceImpl implements GlossaryService {

    private final GlossaryEntryRepository glossaryEntryRepository;

    @Autowired
    public GlossaryServiceImpl(GlossaryEntryRepository glossaryEntryRepository) {
        this.glossaryEntryRepository = glossaryEntryRepository;
    }

    @Override
    public List<GlossaryEntryDto> getGlossaryByLanguage(String language) {
        return glossaryEntryRepository.findByLanguageOrderByTermAsc(language).stream()
                .map(GlossaryEntryDto::new)
                .collect(Collectors.toList());
    }

    @Override
    public List<GlossaryEntryDto> getGlossaryByLanguageAndLetter(String language, String letter) {
        return glossaryEntryRepository.findByLanguageAndLetterOrderByTermAsc(language, letter).stream()
                .map(GlossaryEntryDto::new)
                .collect(Collectors.toList());
    }
}
