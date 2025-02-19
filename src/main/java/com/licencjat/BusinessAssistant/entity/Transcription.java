package com.licencjat.BusinessAssistant.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Table(name="Transcription")
public class Transcription {

    @Id
    @GeneratedValue
    private UUID id;
    @OneToOne
    @JoinColumn(name = "meeting_id", referencedColumnName = "id", nullable = false)
    private Meeting meetingId;

    @Column(name = "transcription_text", columnDefinition = "TEXT")
    private String transcriptionText;
    @Column(name = "generated_at")
    private LocalDateTime generatedAt;
}
