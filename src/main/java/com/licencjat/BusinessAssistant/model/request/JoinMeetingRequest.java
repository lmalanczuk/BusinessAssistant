package com.licencjat.BusinessAssistant.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class JoinMeetingRequest {
    @NotBlank
    private String roomName;

    @NotBlank
    private String userName;
}
