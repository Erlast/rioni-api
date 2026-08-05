package com.rioni.lk.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rioni.lk.api.service.CurrencyService;
import com.rioni.lk.api.service.GlossaryService;
import com.rioni.lk.api.dto.CurrencyDto;
import com.rioni.lk.api.dto.DictionariesResponse;
import com.rioni.lk.api.dto.GlossaryEntryDto;
import java.util.List;

@RestController
@RequestMapping("/api/")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class DictionaryController {

    private final CurrencyService currencyService;
    private final GlossaryService glossaryService;

    @Autowired
    public DictionaryController(CurrencyService currencyService, GlossaryService glossaryService) {
        this.currencyService = currencyService;
        this.glossaryService = glossaryService;
    }

    @GetMapping("/dictionaries/")
    public ResponseEntity<DictionariesResponse> getDictionaries() {
        List<CurrencyDto> currencies = currencyService.getAllCurrencies();
        return new ResponseEntity<>(new DictionariesResponse(currencies), HttpStatus.OK);
    }

    @GetMapping("/dictionaries/glossary/")
    public ResponseEntity<List<GlossaryEntryDto>> getGlossary(
            @RequestParam("lang") String language,
            @RequestParam(value = "letter", required = false) String letter) {
        List<GlossaryEntryDto> glossary = (letter == null || letter.isBlank())
                ? glossaryService.getGlossaryByLanguage(language)
                : glossaryService.getGlossaryByLanguageAndLetter(language, letter);
        return new ResponseEntity<>(glossary, HttpStatus.OK);
    }
}
