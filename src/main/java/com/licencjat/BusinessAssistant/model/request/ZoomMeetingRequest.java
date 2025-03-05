package com.licencjat.BusinessAssistant.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.licencjat.BusinessAssistant.model.ZoomMeetingSettings;
import lombok.Data;

@Data
public class ZoomMeetingRequest {
    private String topic;
    private String type; // 1: instant, 2: scheduled, 3: recurring with no fixed time, 8: recurring with fixed time
    private String start_time; // format: "yyyy-MM-ddTHH:mm:ss"
    private Integer duration; // in minutes
    private String timezone;
    private String agenda;

    @JsonProperty("settings")
    private ZoomMeetingSettings settings;

}