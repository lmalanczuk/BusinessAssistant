package com.licencjat.BusinessAssistant.controller;

import com.licencjat.BusinessAssistant.entity.Transcription;
import com.licencjat.BusinessAssistant.repository.TranscriptionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transcriptions")
public class TranscriptionController {

    private final TranscriptionRepository transcriptionRepository;

    public TranscriptionController(TranscriptionRepository transcriptionRepository) {
        this.transcriptionRepository = transcriptionRepository;
    }

    @GetMapping("/recent")
    public ResponseEntity<List<Transcription>> getRecent() {
        List<Transcription> recent = transcriptionRepository.findTop5ByOrderByGeneratedAtDesc();
        return ResponseEntity.ok(recent);
    }
}
