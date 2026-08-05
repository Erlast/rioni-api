package com.rioni.lk.api.service;

import com.rioni.lk.api.dto.AuthRequest;
import com.rioni.lk.api.dto.AuthResponse;
import com.rioni.lk.api.dto.ChangePasswordRequest;
import com.rioni.lk.api.dto.CheckContactRequest;
import com.rioni.lk.api.dto.CheckContactResponse;
import com.rioni.lk.api.dto.RecoverSmsRequest;
import com.rioni.lk.api.dto.SmsCodeRequest;
import com.rioni.lk.api.dto.SmsCodeResponse;

public interface AuthService {
    AuthResponse authenticate(AuthRequest request);
    AuthResponse checkSmsCode(SmsCodeRequest request);
    AuthResponse refreshToken(int profileId);
    void logout(int profileId);
    CheckContactResponse checkContact(CheckContactRequest request);
    SmsCodeResponse recoverSms(RecoverSmsRequest request);
    void changePassword(ChangePasswordRequest request);
}
