package com.rioni.lk.api.service;

import com.rioni.lk.api.dto.GlossaryEntryDto;
import java.util.List;

public interface GlossaryService {

    List<GlossaryEntryDto> getGlossaryByLanguage(String language);

    List<GlossaryEntryDto> getGlossaryByLanguageAndLetter(String language, String letter);
}
