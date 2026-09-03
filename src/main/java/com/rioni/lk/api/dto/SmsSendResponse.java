package com.rioni.lk.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SmsSendResponse {
    @JsonProperty("purpose")
    private String purpose;

    @JsonProperty("sms_code_id")
    private Long smsCodeId;

    @JsonProperty("phone_masked")
    private String phoneMasked;

    @JsonProperty("profile_id")
    private Integer profileId;
}
