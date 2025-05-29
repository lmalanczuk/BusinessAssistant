package com.licencjat.BusinessAssistant.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TranscriptionDTO {

    @JsonProperty("id")
    private UUID meetingId;
    @JsonProperty("transcriptionText")
    private String transcriptionText;
}
