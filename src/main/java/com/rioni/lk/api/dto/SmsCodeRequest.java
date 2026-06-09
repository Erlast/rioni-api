package com.rioni.lk.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SmsCodeRequest {
    @JsonProperty("sms_code_id")
    private long smsCodeId;
    
    @JsonProperty("code")
    private String code;
}