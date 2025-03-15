package com.licencjat.BusinessAssistant.model.zoom.webhook;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ZoomWebhookPayload {

    @JsonProperty("account_id")
    private String account_id;
    @JsonProperty("object")
    private ZoomWebhookObject object;
}
