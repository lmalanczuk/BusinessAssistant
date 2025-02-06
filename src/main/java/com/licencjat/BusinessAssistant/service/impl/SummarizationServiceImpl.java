package com.licencjat.BusinessAssistant.service.impl;

import com.licencjat.BusinessAssistant.client.OpenAiClient;
import com.licencjat.BusinessAssistant.entity.Meeting;
import com.licencjat.BusinessAssistant.entity.Summary;
import com.licencjat.BusinessAssistant.model.SummaryDTO;
import com.licencjat.BusinessAssistant.model.TranscriptionDTO;
import com.licencjat.BusinessAssistant.repository.MeetingRepository;
import com.licencjat.BusinessAssistant.repository.SummaryRepository;
import com.licencjat.BusinessAssistant.service.SummarizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class SummarizationServiceImpl implements SummarizationService {

    private final OpenAiClient openAiClient;
    private final SummaryRepository summaryRepository;
    private final MeetingRepository meetingRepository;

    @Autowired
    public SummarizationServiceImpl(OpenAiClient openAiClient,
                                    SummaryRepository summaryRepository,
                                    MeetingRepository meetingRepository) {
        this.openAiClient = openAiClient;
        this.summaryRepository = summaryRepository;
        this.meetingRepository = meetingRepository;
    }

    @Override
    public SummaryDTO generateSummary(TranscriptionDTO transcriptionDTO) {
        // Przygotowanie prompta na podstawie tekstu transkrypcji
        String prompt = "Stwórz krótkie podsumowanie rozmowy biznesowej na podstawie poniższej transkrypcji:\n"
                        + transcriptionDTO.getTranscriptionText();

        // Wywołanie klienta OpenAI, który wygeneruje podsumowanie
        String generatedSummary = openAiClient.generateText(prompt);

        // Pobranie encji Meeting na podstawie meetingId z DTO
        UUID meetingId = transcriptionDTO.getMeetingId();
        Optional<Meeting> meetingOpt = meetingRepository.findById(meetingId);
        if (!meetingOpt.isPresent()) {
            throw new IllegalArgumentException("Nie znaleziono spotkania o id " + meetingId);
        }
        Meeting meeting = meetingOpt.get();

        // Utworzenie nowej encji Summary i zapis do bazy danych
        Summary summary = new Summary();
        summary.setMeetingId(meeting);
        summary.setGeneratedSummary(generatedSummary);
        summary.setGeneratedAt(LocalDateTime.now());
        summary = summaryRepository.save(summary);

        // Mapowanie wyniku do DTO
        SummaryDTO summaryDTO = new SummaryDTO();
        summaryDTO.setMeetingId(meeting.getId().toString());
        summaryDTO.setSummaryText(generatedSummary);
        return summaryDTO;
    }
}
