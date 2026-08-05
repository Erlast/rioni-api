package com.rioni.lk.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    @JsonProperty("access_token")
    private String accessToken;
    
    @JsonProperty("token_type")
    private String tokenType = "Bearer";
    
    @JsonProperty("refresh_token")
    private String refreshToken;
    
    @JsonProperty("expires_in")
    private long expiresIn;
    
    @JsonProperty("refresh_token_expires_in")
    private long refreshTokenExpiresIn;
    
    @JsonProperty("sms_code_id")
    private Long smsCodeId;

    @JsonProperty("phone_masked")
    private String phoneMasked;

    @JsonProperty("purpose")
    private String purpose;

    @JsonProperty("profile_id")
    private Integer profileId;
}
