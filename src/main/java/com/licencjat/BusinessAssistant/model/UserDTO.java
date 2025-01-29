package com.licencjat.BusinessAssistant.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.licencjat.BusinessAssistant.entity.enums.Role;
import lombok.Data;

import java.util.UUID;

@Data
public class UserDTO {

    @JsonProperty("id")
    private UUID id;

    @JsonProperty("firstName")
    private String firstName;

    @JsonProperty("lastName")
    private String lastName;

    @JsonProperty("email")
    private String email;

    @JsonProperty("role")
    private Role role;
}
