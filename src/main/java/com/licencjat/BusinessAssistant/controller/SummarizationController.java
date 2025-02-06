package com.licencjat.BusinessAssistant.controller;

import com.licencjat.BusinessAssistant.model.SummaryDTO;
import com.licencjat.BusinessAssistant.model.TranscriptionDTO;
import com.licencjat.BusinessAssistant.service.SummarizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/summaries")
public class SummarizationController {

    private final SummarizationService summarizationService;

    @Autowired
    public SummarizationController(SummarizationService summarizationService) {
        this.summarizationService = summarizationService;
    }

    @PostMapping
    public SummaryDTO generateSummary(@RequestBody TranscriptionDTO transcriptionDTO) {
        return summarizationService.generateSummary(transcriptionDTO);
    }
}
