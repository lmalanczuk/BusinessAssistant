package com.licencjat.BusinessAssistant.webhook.zoom;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.licencjat.BusinessAssistant.client.zoom.ZoomRecordingClient;
import com.licencjat.BusinessAssistant.entity.Meeting;
import com.licencjat.BusinessAssistant.exception.RecordingException;
import com.licencjat.BusinessAssistant.exception.ResourceNotFoundException;
import com.licencjat.BusinessAssistant.model.zoom.ZoomRecordingFile;
import com.licencjat.BusinessAssistant.model.zoom.ZoomRecordingResponse;
import com.licencjat.BusinessAssistant.model.zoom.webhook.ZoomWebhookEvent;
import com.licencjat.BusinessAssistant.repository.MeetingRepository;
import com.licencjat.BusinessAssistant.webhook.WebhookHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;

@Component
public class ZoomRecordingCompletedHandler implements WebhookHandler {
    private static final Logger logger = LoggerFactory.getLogger(ZoomRecordingCompletedHandler.class);
    private static final String RECORDING_DIRECTORY = "recordings";

    private final ObjectMapper objectMapper;
    private final MeetingRepository meetingRepository;
    private final ZoomRecordingClient recordingClient;
    private final RestTemplate restTemplate;

    @Autowired
    public ZoomRecordingCompletedHandler(
            ObjectMapper objectMapper,
            MeetingRepository meetingRepository,
            ZoomRecordingClient recordingClient) {
        this.objectMapper = objectMapper;
        this.meetingRepository = meetingRepository;
        this.recordingClient = recordingClient;
        this.restTemplate = new RestTemplate();

        try {
            Files.createDirectories(Path.of(RECORDING_DIRECTORY));
        } catch(IOException e) {
            logger.error("Error creating recording directory", e);
        }
    }

    @Override
    public boolean canHandle(String eventType) {
        return "recording.completed".equals(eventType);
    }

    @Override
    @Transactional
    public void handle(Map<String, Object> webhookData) {
        try {
            ZoomWebhookEvent event = objectMapper.convertValue(webhookData, ZoomWebhookEvent.class);
            String meetingId = event.getPayload().getObject().getId();
            logger.info("Recording completed for meeting: {}", meetingId);

            JsonNode recordingData = recordingClient.getMeetingRecordings(meetingId);
            ZoomRecordingResponse recordingResponse = objectMapper.treeToValue(
                    recordingData,
                    ZoomRecordingResponse.class
            );

            Optional<Meeting> meetingOptional = findMeetingByZoomId(meetingId);
            if (!meetingOptional.isPresent()) {
                throw new ResourceNotFoundException("Nie znaleziono spotkania o id: " + meetingId);
            }

            Meeting meeting = meetingOptional.get();

            for (ZoomRecordingFile file : recordingResponse.getRecordingFiles()) {
                if ("TRANSCRIPT".equals(file.getRecordingType()) || "AUDIO_ONLY".equals(file.getRecordingType())) {
                    String downloadUrl = file.getDownloadUrl();
                    String fileName = String.format("%s_%s.%s",
                            meeting.getId(),
                            file.getRecordingType().toLowerCase(),
                            getFileExtension(file.getFileType()));
                    Path filePath = downloadFile(downloadUrl, fileName);

                    if ("AUDIO_ONLY".equals(file.getRecordingType())) {
                        meeting.setZoomRecordingUrl(filePath.toString());
                        meetingRepository.save(meeting);
                        logger.info("Zaktualizowano nagranie audio dla spotkania {}: {}", meetingId, filePath);
                    }
                }
            }
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error processing recording", e);
            throw new RecordingException("Błąd przetwarzania nagrania", e);
        }
    }

    private Optional<Meeting> findMeetingByZoomId(String zoomMeetingId) {
        return meetingRepository.findAll().stream()
                .filter(m -> zoomMeetingId.equals(m.getZoomMeetingId()))
                .findFirst();
    }

    private String getFileExtension(String fileType) {
        if (fileType.equals("MP4")) return "mp4";
        if (fileType.equals("M4A")) return "m4a";
        if (fileType.equals("VTT")) return "vtt";
        if (fileType.equals("CHAT")) return "txt";
        return "bin";
    }

    private Path downloadFile(String url, String fileName) throws IOException {
        Path filePath = Paths.get(RECORDING_DIRECTORY, fileName);

        byte[] fileContent = restTemplate.getForObject(URI.create(url), byte[].class);
        if (fileContent != null) {
            Files.write(filePath, fileContent);
            return filePath;
        }
        throw new IOException("Error downloading file");
    }
}