package com.licencjat.BusinessAssistant.controller;

import com.licencjat.BusinessAssistant.model.Recording;
import com.licencjat.BusinessAssistant.service.RecordingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recordings")
public class RecordingController {

    private final RecordingService recordingService;

    public RecordingController(RecordingService recordingService) {
        this.recordingService = recordingService;
    }

    // GET /api/recordings/recent
    @GetMapping("/recent")
    public ResponseEntity<List<Recording>> getRecent() {
        List<Recording> list = recordingService.getRecentRecordings();
        return ResponseEntity.ok(list);
    }
}
