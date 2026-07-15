package com.rioni.lk.api.exception;

public class SmsCodeNotFoundException extends RuntimeException {

    public SmsCodeNotFoundException(String message) {
        super(message);
    }
}
