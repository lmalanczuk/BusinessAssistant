package com.licencjat.BusinessAssistant.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TokenResponse {
    @JsonProperty("token")
    private String token;

    @JsonProperty("message")
    private String message;
}