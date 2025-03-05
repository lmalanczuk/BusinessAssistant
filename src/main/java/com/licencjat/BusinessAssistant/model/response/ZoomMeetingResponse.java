package com.licencjat.BusinessAssistant.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ZoomMeetingResponse {
    private String id;
    private String topic;
    private int type;

    @JsonProperty("start_time")
    private String startTime;

    private int duration;
    private String timezone;

    @JsonProperty("join_url")
    private String joinUrl;

    @JsonProperty("start_url")
    private String startUrl;

    @JsonProperty("host_id")
    private String hostId;
}