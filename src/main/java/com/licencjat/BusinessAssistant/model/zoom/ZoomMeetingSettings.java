//package com.licencjat.BusinessAssistant.model.zoom;
//
//import com.fasterxml.jackson.annotation.JsonInclude;
//import com.fasterxml.jackson.annotation.JsonProperty;
//import lombok.AllArgsConstructor;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//@JsonInclude(JsonInclude.Include.NON_NULL)
//public class ZoomMeetingSettings {
//    @JsonProperty("host_video")
//    private Boolean hostVideo;
//    @JsonProperty("participant_video")
//    private Boolean participantVideo;
//    @JsonProperty("join_before_host")
//    private Boolean joinBeforeHost;
//    @JsonProperty("mute_upon_entry")
//    private Boolean muteUponEntry;
//    @JsonProperty("auto_recording")
//    private String autoRecording; // local, cloud, none
//    @JsonProperty("waiting_room")
//    private Boolean waitingRoom;
//
//    public static Builder builder() {
//        return new Builder();
//    }
//
//    // Custom builder since Lombok's might not be working
//    public static class Builder {
//        private Boolean hostVideo;
//        private Boolean participantVideo;
//        private Boolean joinBeforeHost;
//        private Boolean muteUponEntry;
//        private String autoRecording;
//        private Boolean waitingRoom;
//
//        public Builder hostVideo(Boolean hostVideo) {
//            this.hostVideo = hostVideo;
//            return this;
//        }
//
//        public Builder participantVideo(Boolean participantVideo) {
//            this.participantVideo = participantVideo;
//            return this;
//        }
//
//        public Builder joinBeforeHost(Boolean joinBeforeHost) {
//            this.joinBeforeHost = joinBeforeHost;
//            return this;
//        }
//
//        public Builder muteUponEntry(Boolean muteUponEntry) {
//            this.muteUponEntry = muteUponEntry;
//            return this;
//        }
//
//        public Builder autoRecording(String autoRecording) {
//            this.autoRecording = autoRecording;
//            return this;
//        }
//
//        public Builder waitingRoom(Boolean waitingRoom) {
//            this.waitingRoom = waitingRoom;
//            return this;
//        }
//
//        public ZoomMeetingSettings build() {
//            return new ZoomMeetingSettings(
//                    hostVideo,
//                    participantVideo,
//                    joinBeforeHost,
//                    muteUponEntry,
//                    autoRecording,
//                    waitingRoom);
//        }
//    }
//}