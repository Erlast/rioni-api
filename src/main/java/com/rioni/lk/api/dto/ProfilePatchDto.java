package com.rioni.lk.api.dto;

import java.util.HashMap;
import java.util.Map;

public class ProfilePatchDto {
    private Map<String, Object> fields = new HashMap<>();

    public void set(String field, Object value) {
        fields.put(field, value);
    }

    public Object get(String field) {
        return fields.get(field);
    }

    public boolean has(String field) {
        return fields.containsKey(field);
    }

    public Map<String, Object> getFields() {
        return fields;
    }
}