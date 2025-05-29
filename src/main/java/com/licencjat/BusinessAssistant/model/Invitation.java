package com.licencjat.BusinessAssistant.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.checkerframework.checker.index.qual.SearchIndexBottom;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Invitation {
    private String id;
    private String meetingTitle;
    private String senderName;


}
