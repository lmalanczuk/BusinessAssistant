package com.licencjat.BusinessAssistant.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class CreateZoomMeetingRequest {

    @JsonProperty("title")
    private String title;
    @JsonProperty("start_time")
    private LocalDateTime startTime;
    @JsonProperty("duration_minutes")
    private int durationMinutes;
    @JsonProperty("host_user_id")
    private UUID hostUserId;

}
