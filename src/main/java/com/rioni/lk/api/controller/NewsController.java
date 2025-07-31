package com.rioni.lk.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.rioni.lk.api.service.NewsService;
import com.rioni.lk.api.dto.NewsResponse;

@RestController
@RequestMapping("/api/")

public class NewsController {

    private final NewsService newsService;

    @Autowired
    public NewsController(NewsService newsService) {
        this.newsService = newsService;
    }

    @GetMapping("news")
    public ResponseEntity<NewsResponse> getNews() {
        return new ResponseEntity<>(newsService.getAll(), HttpStatus.OK);
    }
}