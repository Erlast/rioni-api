package com.rioni.lk.api.dto;

import com.rioni.lk.api.model.GlossaryEntry;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GlossaryEntryDto {
    private int id;
    private int sourceNo;
    private String language;
    private String letter;
    private String term;
    private String english;
    private String definition;

    public GlossaryEntryDto(GlossaryEntry entry) {
        this.id = entry.getId();
        this.sourceNo = entry.getSourceNo();
        this.language = entry.getLanguage();
        this.letter = entry.getLetter();
        this.term = entry.getTerm();
        this.english = entry.getEnglish();
        this.definition = entry.getDefinition();
    }
}
