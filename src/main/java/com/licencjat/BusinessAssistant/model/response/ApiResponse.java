package com.licencjat.BusinessAssistant.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse {
    @JsonProperty("success")
    private Boolean success;
    @JsonProperty("message")
    private String message;
}
