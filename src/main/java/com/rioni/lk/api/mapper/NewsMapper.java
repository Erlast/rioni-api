package com.rioni.lk.api.mapper;

import lombok.NoArgsConstructor;
import com.rioni.lk.api.dto.NewsDto;
import com.rioni.lk.api.model.News;

@NoArgsConstructor
public class NewsMapper {
    public static NewsDto mapToDto(News news) {
        NewsDto newsDto = new NewsDto();
        newsDto.setId(news.getId());
        newsDto.setTitle(news.getTitle());
        newsDto.setUrl(news.getUrl());
        newsDto.setText(news.getText());
        return newsDto;
    }

}