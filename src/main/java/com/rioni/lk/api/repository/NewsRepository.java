package com.rioni.lk.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.rioni.lk.api.model.News;

public interface NewsRepository extends JpaRepository<News, Integer> {

}
