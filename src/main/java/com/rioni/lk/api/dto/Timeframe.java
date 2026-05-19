package com.rioni.lk.api.dto;

public enum Timeframe {
    WEEK("week"),
    MONTH("month"),
    SIX_MONTHS("sixMonths"),
    FROM_YEAR("fromYear"),
    YEAR("year"),
    ALL_PERIOD("allPeriod");

    private final String value;

    Timeframe(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static Timeframe fromValue(String value) {
        for (Timeframe timeframe : Timeframe.values()) {
            if (timeframe.value.equalsIgnoreCase(value)) {
                return timeframe;
            }
        }
        return WEEK;
    }
}
