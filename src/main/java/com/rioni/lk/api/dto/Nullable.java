package com.rioni.lk.api.dto;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import lombok.Getter;

import java.io.IOException;

public class Nullable<T> {
    @Getter
    private final T value;
    private final boolean sent;

    public Nullable(T value, boolean sent) {
        this.value = value;
        this.sent = sent;
    }

    public static <T> Nullable<T> of(T value) {
        return new Nullable<>(value, true);
    }

    public boolean wasSent() {
        return sent;
    }

    @JsonSerialize(using = NullableSerializer.class)
    public static class NullableSerializer extends StdSerializer<Nullable> {
        public NullableSerializer() {
            super(Nullable.class);
        }

        @Override
        public void serialize(Nullable value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            if (value == null) {
                gen.writeNull();
            } else {
                gen.writeObject(value.getValue());
            }
        }
    }
}