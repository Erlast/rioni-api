package com.rioni.lk.api.exception;

public class TariffNotFoundException extends RuntimeException {
    public TariffNotFoundException(String message) {
        super(message);
    }
}
