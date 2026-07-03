package com.rioni.lk.api.service;

import com.rioni.lk.api.dto.AuthRequest;
import com.rioni.lk.api.dto.AuthResponse;
import com.rioni.lk.api.dto.SmsCodeResponse;

public interface AuthService {
    AuthResponse authenticate(AuthRequest request);
    SmsCodeResponse sendSmsCode(String phone);
    AuthResponse checkSmsCode(long smsCodeId, String code);
    AuthResponse refreshToken(int profileId);
    void logout(int profileId);
}
