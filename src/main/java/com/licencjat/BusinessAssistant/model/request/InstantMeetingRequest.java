package com.licencjat.BusinessAssistant.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class InstantMeetingRequest {
    @JsonProperty("title")
    private String title;

    @JsonProperty("isInstant")
    private boolean isInstant;

    @JsonProperty("durationMinutes")
    private Integer durationMinutes = 60;
}