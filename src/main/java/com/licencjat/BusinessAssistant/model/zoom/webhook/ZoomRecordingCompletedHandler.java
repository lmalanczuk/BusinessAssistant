package com.licencjat.BusinessAssistant.model.zoom.webhook;

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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Optional;

@Component
public class ZoomRecordingCompletedHandler implements WebhookHandler {
    private static final Logger logger = LoggerFactory.getLogger(ZoomRecordingCompletedHandler.class);

    @Value("${app.recordings.directory:recordings}")
    private String recordingsDirectory;

    private final ObjectMapper objectMapper;
    private final MeetingRepository meetingRepository;
    private final ZoomRecordingClient recordingClient;
    private final RestTemplate restTemplate;
    private Path recordingsPath;

    @Autowired
    public ZoomRecordingCompletedHandler(
            ObjectMapper objectMapper,
            MeetingRepository meetingRepository,
            ZoomRecordingClient recordingClient) {
        this.objectMapper = objectMapper;
        this.meetingRepository = meetingRepository;
        this.recordingClient = recordingClient;
        this.restTemplate = new RestTemplate();
    }

    @PostConstruct
    public void init() {
        try {
            // Default to "recordings" if the property is not set
            if (recordingsDirectory == null || recordingsDirectory.isEmpty()) {
                recordingsDirectory = "recordings";
                logger.warn("recordings.directory not configured, using default: {}", recordingsDirectory);
            }

            this.recordingsPath = Paths.get(recordingsDirectory);
            Files.createDirectories(recordingsPath);
            logger.info("Recordings directory created: {}", recordingsPath.toAbsolutePath());
        } catch(IOException e) {
            logger.error("Error creating recording directory", e);
            throw new RuntimeException("Could not create recordings directory", e);
        }
    }

    @Override
    public boolean canHandle(String eventType) {
        return "recording.completed".equals(eventType);
    }

    @Override
    @Transactional
    public void handle(Map<String, Object> webhookData) {
        ZoomWebhookEvent event = null;
        try {
            event = objectMapper.convertValue(webhookData, ZoomWebhookEvent.class);
            String meetingId = event.getPayload().getObject().getId();
            logger.info("Recording completed for meeting: {}", meetingId);

            JsonNode recordingData = recordingClient.getMeetingRecordings(meetingId);
            ZoomRecordingResponse recordingResponse = objectMapper.treeToValue(
                    recordingData,
                    ZoomRecordingResponse.class
            );

            Optional<Meeting> meetingOptional = meetingRepository.findByZoomMeetingId(meetingId);
            if (!meetingOptional.isPresent()) {
                throw new ResourceNotFoundException("Meeting not found with ID: " + meetingId);
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
                        logger.info("Updated audio recording for meeting {}: {}", meetingId, filePath);
                    }
                }
            }
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            String meetingId = event != null ? event.getPayload().getObject().getId() : "unknown";
            logger.error("Error processing recording for meeting {}", meetingId, e);
            throw new RecordingException("Error processing recording: " + e.getMessage(), e);
        }
    }

    private String getFileExtension(String fileType) {
        if (fileType.equals("MP4")) return "mp4";
        if (fileType.equals("M4A")) return "m4a";
        if (fileType.equals("VTT")) return "vtt";
        if (fileType.equals("CHAT")) return "txt";
        return "bin";
    }

    private Path downloadFile(String url, String fileName) throws IOException {
        Path filePath = recordingsPath.resolve(fileName);
        logger.info("Downloading recording to: {}", filePath.toAbsolutePath());

        // Using try-with-resources to ensure streams are closed
        try (InputStream in = restTemplate.execute(URI.create(url),
                org.springframework.http.HttpMethod.GET, null,
                clientHttpResponse -> clientHttpResponse.getBody())) {

            if (in == null) {
                throw new IOException("Error downloading file: empty response");
            }

            Files.copy(in, filePath, StandardCopyOption.REPLACE_EXISTING);
            return filePath;
        } catch (Exception e) {
            logger.error("Error downloading recording", e);
            throw new IOException("Error downloading file: " + e.getMessage(), e);
        }
    }
}