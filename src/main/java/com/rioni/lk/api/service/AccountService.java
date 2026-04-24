package com.rioni.lk.api.service;

import com.rioni.lk.api.dto.AccountResponse;

public interface AccountService {
     AccountResponse getAllAccountsByProfileId(Long profileId);
}
