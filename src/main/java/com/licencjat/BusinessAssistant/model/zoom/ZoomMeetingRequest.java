//package com.licencjat.BusinessAssistant.model.zoom;
//
//import com.fasterxml.jackson.annotation.JsonInclude;
//import com.fasterxml.jackson.annotation.JsonProperty;
//import lombok.Builder;
//import lombok.Data;
//
//@Data
//@Builder
//@JsonInclude(JsonInclude.Include.NON_NULL)
//public class ZoomMeetingRequest {
//    @JsonProperty("topic")
//    private String topic;
//    @JsonProperty("type")
//    private int type; //1: instant, 2: scheduled, 3: recurring with no fixed time, 8: recurring with fixed time
//    @JsonProperty("start_time")
//    private String startTime;
//    @JsonProperty("duration")
//    private int duration;
//    @JsonProperty("timezone")
//    private String timezone;
//    @JsonProperty("password")
//    private String password;
//    @JsonProperty("agenda")
//    private String agenda;
//    @JsonProperty("settings")
//    private ZoomMeetingSettings settings;
//}
