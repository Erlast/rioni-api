package com.rioni.lk.api.dto;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import java.io.IOException;

public class NullableDeserializer extends JsonDeserializer<Nullable<?>> {
    @Override
    public Nullable<?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        if (node.isNull() || node.isMissingNode()) {
            return new Nullable<>(null, true);
        }
        Object value;
        if (node.isTextual()) {
            value = node.asText();
        } else if (node.isNumber()) {
            value = node.numberValue();
        } else if (node.isBoolean()) {
            value = node.asBoolean();
        } else {
            value = node.toString();
        }
        return new Nullable<>(value, true);
    }
}