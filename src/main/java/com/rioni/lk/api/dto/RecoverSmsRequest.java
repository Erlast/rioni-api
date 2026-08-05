package com.rioni.lk.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecoverSmsRequest {
    @JsonProperty("value")
    private String value;

    @JsonProperty("type")
    private String type;
}
