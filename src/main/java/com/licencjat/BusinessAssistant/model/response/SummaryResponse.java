package com.licencjat.BusinessAssistant.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SummaryResponse {
    @JsonProperty("summaryText")
    private String summaryText;
}
