package com.licencjat.BusinessAssistant.repository;

import com.licencjat.BusinessAssistant.entity.Transcription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TranscriptionRepository extends JpaRepository<Transcription, UUID> {
}
