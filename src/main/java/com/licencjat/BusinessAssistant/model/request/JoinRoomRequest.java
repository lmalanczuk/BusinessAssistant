package com.licencjat.BusinessAssistant.model.request;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class JoinRoomRequest {
    @JsonProperty("roomId")
    private String roomId;
}