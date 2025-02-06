package com.licencjat.BusinessAssistant.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
public class Summary {

    @Id
    @GeneratedValue
    private UUID id;
    @OneToOne
    @JoinColumn(name = "meeting_id", referencedColumnName = "id", nullable = false)
    private Meeting meetingId;
    @Column(name = "generated_summary", columnDefinition = "TEXT")
    private String generatedSummary;
    @Column(name = "generated_at")
    private LocalDateTime generatedAt;
}
