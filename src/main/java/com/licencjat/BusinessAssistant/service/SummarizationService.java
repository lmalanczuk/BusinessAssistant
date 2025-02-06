package com.licencjat.BusinessAssistant.service;

import com.licencjat.BusinessAssistant.model.SummaryDTO;
import com.licencjat.BusinessAssistant.model.TranscriptionDTO;

public interface SummarizationService {
    SummaryDTO generateSummary(TranscriptionDTO transcriptionDTO);
}
