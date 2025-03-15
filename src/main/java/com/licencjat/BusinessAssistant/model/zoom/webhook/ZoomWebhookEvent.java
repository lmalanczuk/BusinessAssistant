package com.licencjat.BusinessAssistant.model.zoom.webhook;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ZoomWebhookEvent {
    @JsonProperty("event")
    private String event;
    @JsonProperty("payload")
    private ZoomWebhookPayload payload;
    @JsonProperty("event_ts")
    private long timestamp;
}
