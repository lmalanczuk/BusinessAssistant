package com.licencjat.BusinessAssistant.model.zoom;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
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
    private String autoRecording; // local, cloud, none
    @JsonProperty("waiting_room")
    private Boolean waitingRoom;
}
