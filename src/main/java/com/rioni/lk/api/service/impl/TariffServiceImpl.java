package com.rioni.lk.api.service.impl;

import com.rioni.lk.api.dto.TariffDto;
import com.rioni.lk.api.repository.TariffRepository;
import com.rioni.lk.api.service.TariffService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TariffServiceImpl implements TariffService {

    private final TariffRepository tariffRepository;

    @Autowired
    public TariffServiceImpl(TariffRepository tariffRepository) {
        this.tariffRepository = tariffRepository;
    }

    @Override
    public List<TariffDto> getAllTariffs() {
        return tariffRepository.findAll().stream()
                .map(TariffDto::new)
                .collect(Collectors.toList());
    }
}
