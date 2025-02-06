package com.licencjat.BusinessAssistant.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SummaryResponse {
    @JsonProperty("summaryText")
    private String summaryText;

    public SummaryResponse(String summaryText) {
        this.summaryText = summaryText;
    }
}