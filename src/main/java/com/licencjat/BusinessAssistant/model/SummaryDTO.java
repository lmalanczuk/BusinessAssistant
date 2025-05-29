package com.licencjat.BusinessAssistant.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SummaryDTO {
        @JsonProperty("meetingId")
        private String meetingId;
        @JsonProperty("summaryText")
        private String summaryText;
}
