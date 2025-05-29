package com.licencjat.BusinessAssistant.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Recording {
    private String id;
    private String meetingTitle;
    private String recordedAt;  // ISO date string
    private String url;
}
