package com.rioni.lk.api.service.impl;

import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import com.rioni.lk.api.dto.NewsResponse;
import com.rioni.lk.api.dto.NewsDto;
import com.rioni.lk.api.service.NewsService;
import com.rioni.lk.api.repository.NewsRepository;
import com.rioni.lk.api.model.News;
import java.util.List;
import com.rioni.lk.api.mapper.NewsMapper;
import org.springframework.stereotype.Service;

@Service
public class NewsServiceImpl implements NewsService {
    private final NewsRepository newsRepository;

    @Autowired
    public NewsServiceImpl(NewsRepository newsRepository) {
        this.newsRepository = newsRepository;
    }

    @Override
    public NewsResponse getAll() {

        List<News> listNews = newsRepository.findAll();

        List<NewsDto> content = listNews.stream().map(NewsMapper::mapToDto).collect(Collectors.toList());

        NewsResponse newsResponse = new NewsResponse();
        newsResponse.setContent(content);

        return newsResponse;
    }
}