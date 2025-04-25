package com.licencjat.BusinessAssistant.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.licencjat.BusinessAssistant.model.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class AuthResponse {
    @JsonProperty("token")
    private String token;
     @JsonProperty("user")
    private UserDTO user;

}
