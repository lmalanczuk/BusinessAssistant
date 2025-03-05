package com.licencjat.BusinessAssistant.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ZoomMeetingSettings {
    @JsonProperty("host_video")
    private Boolean hostVideo;

    @JsonProperty("participant_video")
    private Boolean participantVideo;

    @JsonProperty("join_before_host")
    private Boolean joinBeforeHost;

    @JsonProperty("mute_upon_entry")
    private Boolean muteUponEntry;

    @JsonProperty("auto_recording")
    private String autoRecording; // "none", "local", "cloud"
}