package com.licencjat.BusinessAssistant.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.UUID;

@Data
public class TranscriptionDTO {

    @JsonProperty("id")
    private UUID meetingId;
    @JsonProperty("transcriptionText")
    private String transcriptionText;
}
