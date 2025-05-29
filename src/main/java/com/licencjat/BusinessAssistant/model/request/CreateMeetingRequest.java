package com.licencjat.BusinessAssistant.model.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;


public class CreateMeetingRequest {

    @NotNull
    private String title;

    @NotNull
    private LocalDateTime startTime;

    @Min(1)
    private int durationMinutes;

    // Usuń getter/setter i pole hostUserId

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }
}
