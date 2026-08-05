package com.rioni.lk.api.exception;

import lombok.Getter;

@Getter
public class SmsCodeAttemptsExceededException extends RuntimeException {
    private final long timeLeft;

    public SmsCodeAttemptsExceededException(String message, long timeLeft) {
        super(message);
        this.timeLeft = timeLeft;
    }
}
