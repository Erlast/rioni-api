package com.rioni.lk.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body for the profile registration endpoint.
 * <p>
 * JSON contract:
 * <pre>
 * {
 *   "name": "string",
 *   "login": "string (optional)",
 *   "phone": "string",
 *   "email": "string",
 *   "password": "string",
 *   "confirm_password": "string",
 *   "privacy_policy": boolean,
 *   "confirm_adds": boolean
 * }
 * </pre>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegistrationRequest {

    private String name;

    /** Optional; when absent the email is used as the fallback login. */
    private String login;

    private String phone;

    private String email;

    private String password;

    @JsonProperty("confirm_password")
    private String confirmPassword;

    @JsonProperty("privacy_policy")
    private boolean privacyPolicy;

    @JsonProperty("confirm_adds")
    private boolean confirmAdds;
}
