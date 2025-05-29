// MeetingTokenResponse.java
package com.licencjat.BusinessAssistant.model.response;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MeetingTokenResponse {
    private String token;
    private String roomUrl;
    private String roomName;
}