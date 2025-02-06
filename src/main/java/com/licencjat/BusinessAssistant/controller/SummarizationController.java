package com.licencjat.BusinessAssistant.controller;

import com.licencjat.BusinessAssistant.model.request.SummaryRequest;
import com.licencjat.BusinessAssistant.model.response.SummaryResponse;
import com.licencjat.BusinessAssistant.service.SummarizationService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/summaries")
public class SummarizationController {

    private final SummarizationService summarizationService;

    public SummarizationController(SummarizationService summarizationService) {
        this.summarizationService = summarizationService;
    }

    @PostMapping(value = "/generate", produces = MediaType.APPLICATION_JSON_VALUE)
    public SummaryResponse generateSummary(@RequestBody SummaryRequest request) {
        return summarizationService.generateSummaryFromText(request);
    }
}