package com.rioni.lk.api.dto;

import lombok.Data;

import java.util.List;

@Data
public class NewsResponse {
    private List<NewsDto> content;

}