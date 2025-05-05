package com.licencjat.BusinessAssistant.model.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateMeetingRequest {
    @NotBlank(message = "Tytuł nie może być pusty")
    @JsonProperty("title")
    private String title;

    @NotNull(message = "Czas rozpoczęcia nie może być pusty")
    @JsonProperty("start_time")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startTime;

    @Min(value = 1, message = "Czas trwania musi być większy niż 0")
    @JsonProperty("duration_minutes")
    private int durationMinutes;
}