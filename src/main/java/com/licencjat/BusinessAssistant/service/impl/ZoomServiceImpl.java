package com.licencjat.BusinessAssistant.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.licencjat.BusinessAssistant.client.ZoomClient;
import com.licencjat.BusinessAssistant.entity.Meeting;
import com.licencjat.BusinessAssistant.entity.Users;
import com.licencjat.BusinessAssistant.entity.enums.Platform;
import com.licencjat.BusinessAssistant.entity.enums.Status;
import com.licencjat.BusinessAssistant.exception.RecordingException;
import com.licencjat.BusinessAssistant.exception.ResourceNotFoundException;
import com.licencjat.BusinessAssistant.exception.ZoomApiException;
import com.licencjat.BusinessAssistant.model.zoom.ZoomMeetingRequest;
import com.licencjat.BusinessAssistant.model.zoom.ZoomMeetingSettings;
import com.licencjat.BusinessAssistant.model.zoom.ZoomRecordingFile;
import com.licencjat.BusinessAssistant.model.zoom.ZoomRecordingResponse;
import com.licencjat.BusinessAssistant.model.zoom.webhook.ZoomWebhookEvent;
import com.licencjat.BusinessAssistant.repository.MeetingRepository;
import com.licencjat.BusinessAssistant.repository.UserRepository;
import com.licencjat.BusinessAssistant.service.ZoomService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class ZoomServiceImpl implements ZoomService {

    private static final Logger logger = LoggerFactory.getLogger(ZoomServiceImpl.class);
    private static final String RECORDING_DIRECTORY = "recordings";

    private final ZoomClient zoomClient;
    private final MeetingRepository meetingRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    @Autowired
    public ZoomServiceImpl(ZoomClient zoomClient,
                      MeetingRepository meetingRepository,
                      UserRepository userRepository) {
        this.zoomClient = zoomClient;
        this.meetingRepository = meetingRepository;
        this.userRepository = userRepository;
        this.objectMapper = new ObjectMapper();
        this.restTemplate = new RestTemplate();
        try{
        Files.createDirectories(Path.of(RECORDING_DIRECTORY));
        }
        catch(IOException e){
        logger.error("Error creating recording directory", e);
        }
    }



    @Override
    public void processWebhook(Map<String, Object> webhookData) {
    try{
        ZoomWebhookEvent event = objectMapper.convertValue(webhookData, ZoomWebhookEvent.class);
        switch (event.getEvent()) {
            case "meeting.started":
                handleMeetingStarted(event);
                break;
        case "meeting.ended":
            handleMeetingEnded(event);
            break;
        case "recording.completed":
            handleRecordingCompleted(event);
            break;
        default:
            logger.warn("Unknown event type: {}", event.getEvent());
        }
    } catch(Exception e){
        logger.error("Error processing webhook", e);
        throw new RuntimeException("Błąd przetwarzania webhooka");
    }
    }


    @Override
    public void downloadRecordingsForMeeting(String meetingId) {

    }

    /**
     * Obsługa rozpoczęcia spotkania
     */
    private void handleMeetingStarted(ZoomWebhookEvent event) {
      String meetingId = event.getPayload().getObject().getId();
      String hostId = event.getPayload().getObject().getHostId();

      logger.info("Meeting started: {}", meetingId);

        Optional <Meeting> meetingOptional = findMeetingByZoomId(meetingId);

        if(meetingOptional.isPresent()){
            Meeting meeting = meetingOptional.get();
            meeting.setStatus(Status.ONGOING);
            meeting.setStartTime(LocalDateTime.now());
            meetingRepository.save(meeting);
            logger.info("Zaktualizowano status spotkania na trwające");
        }else{
            logger.warn("Nie znaleziono spotkania o id: {}", meetingId);
        }

    }
    /**
     * Obsługa zakończenia spotkania
     */
    private void handleMeetingEnded(ZoomWebhookEvent event) {
        String meetingId = event.getPayload().getObject().getId();

        logger.info("Meeting ended: {}", meetingId);

        Optional<Meeting> meetingOptional = findMeetingByZoomId(meetingId);

        if(meetingOptional.isPresent()){
            Meeting meeting = meetingOptional.get();
            meeting.setStatus(Status.COMPLETED);
            meeting.setEndTime(LocalDateTime.now());
            meetingRepository.save(meeting);
            logger.info("Zaktualizowano status spotkania na zakończone");
        }else{
            logger.warn("Nie znaleziono spotkania o id: {}", meetingId);
        }
    }
    @Override
public Meeting startMeeting(String meetingId) {
    try {
        // Znajdź spotkanie w bazie danych
        Optional<Meeting> meetingOptional = findMeetingByZoomId(meetingId);

        if (!meetingOptional.isPresent()) {
            throw new ResourceNotFoundException("Nie znaleziono spotkania o ID: " + meetingId);
        }

        Meeting meeting = meetingOptional.get();

        // Wywołaj API Zoom do rozpoczęcia spotkania
        zoomClient.startMeeting(meetingId);

        // Aktualizuj status spotkania w bazie danych
        meeting.setStatus(Status.ONGOING);
        meeting.setStartTime(LocalDateTime.now());

        return meetingRepository.save(meeting);
    } catch (ResourceNotFoundException e) {
        // Przekaż wyjątek dalej
        throw e;
    } catch (ZoomApiException e) {
        // Przekaż wyjątek dalej
        throw e;
    } catch (Exception e) {
        logger.error("Błąd podczas rozpoczynania spotkania: {}", e.getMessage());
        throw new ZoomApiException("Nie można rozpocząć spotkania", e);
    }
}

@Override
public Meeting endMeeting(String meetingId) {
    try {
        // Znajdź spotkanie w bazie danych
        Optional<Meeting> meetingOptional = findMeetingByZoomId(meetingId);

        if (!meetingOptional.isPresent()) {
            throw new IllegalArgumentException("Nie znaleziono spotkania o ID: " + meetingId);
        }

        Meeting meeting = meetingOptional.get();

        // Wywołaj API Zoom do zakończenia spotkania
        zoomClient.endMeeting(meetingId);

        // Aktualizuj status spotkania w bazie danych
        meeting.setStatus(Status.COMPLETED);
        meeting.setEndTime(LocalDateTime.now());

        return meetingRepository.save(meeting);
    } catch (Exception e) {
        logger.error("Błąd podczas kończenia spotkania: {}", e.getMessage());
        throw new RuntimeException("Nie można zakończyć spotkania", e);
    }
}

@Override
public JsonNode getMeetingParticipants(String meetingId) {
    try {
        // Sprawdź, czy spotkanie istnieje w bazie danych
        Optional<Meeting> meetingOptional = findMeetingByZoomId(meetingId);

        if (!meetingOptional.isPresent()) {
            throw new IllegalArgumentException("Nie znaleziono spotkania o ID: " + meetingId);
        }

        // Wywołaj API Zoom do pobrania uczestników
        return zoomClient.getMeetingParticipants(meetingId);
    } catch (Exception e) {
        logger.error("Błąd podczas pobierania uczestników spotkania: {}", e.getMessage());
        throw new RuntimeException("Nie można pobrać uczestników spotkania", e);
    }
}

@Override
public JsonNode inviteToMeeting(String meetingId, List<String> emails) {
    try {
        // Sprawdź, czy spotkanie istnieje w bazie danych
        Optional<Meeting> meetingOptional = findMeetingByZoomId(meetingId);

        if (!meetingOptional.isPresent()) {
            throw new IllegalArgumentException("Nie znaleziono spotkania o ID: " + meetingId);
        }

        // Wywołaj API Zoom do wysłania zaproszeń
        return zoomClient.inviteToMeeting(meetingId, emails);
    } catch (Exception e) {
        logger.error("Błąd podczas wysyłania zaproszeń: {}", e.getMessage());
        throw new RuntimeException("Nie można wysłać zaproszeń", e);
    }
}

@Override
public Meeting updateMeeting(String meetingId, Map<String, Object> updateData) {
    try {
        // Znajdź spotkanie w bazie danych
        Optional<Meeting> meetingOptional = findMeetingByZoomId(meetingId);

        if (!meetingOptional.isPresent()) {
            throw new IllegalArgumentException("Nie znaleziono spotkania o ID: " + meetingId);
        }

        Meeting meeting = meetingOptional.get();

        // Wywołaj API Zoom do aktualizacji spotkania
        JsonNode updatedMeetingData = zoomClient.updateMeeting(meetingId, updateData);

        // Aktualizuj dane spotkania w bazie danych
        if (updateData.containsKey("topic")) {
            meeting.setTitle((String) updateData.get("topic"));
        }

        if (updateData.containsKey("start_time")) {
            String startTimeStr = (String) updateData.get("start_time");
            meeting.setStartTime(LocalDateTime.parse(startTimeStr, DateTimeFormatter.ISO_DATE_TIME));
        }

        if (updateData.containsKey("duration")) {
            Integer durationMinutes = (Integer) updateData.get("duration");
            meeting.setEndTime(meeting.getStartTime().plusMinutes(durationMinutes));
        }

        return meetingRepository.save(meeting);
    } catch (Exception e) {
        logger.error("Błąd podczas aktualizacji spotkania: {}", e.getMessage());
        throw new RuntimeException("Nie można zaktualizować spotkania", e);
    }
}
    /**
     * Obsługa zakończenia nagrania
     */
    private void handleRecordingCompleted(ZoomWebhookEvent event) {
    String meetingId = event.getPayload().getObject().getId();
    logger.info("Recording completed for meeting: {}", meetingId);
    try {
        JsonNode recordingData = zoomClient.getMeetingRecordings(meetingId);
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
        // Przekaż wyjątek dalej
        throw e;
    } catch (Exception e) {
        logger.error("Error processing recording", e);
        throw new RecordingException("Błąd przetwarzania nagrania", e);
    }
}
    @Override
    public Meeting createZoomMeeting(String title, LocalDateTime startTime, int durationMinutes, UUID hostUserId){
        try{
         Optional<Users> hostOpt = userRepository.findById(hostUserId);
         if(!hostOpt.isPresent()){
             throw new IllegalArgumentException("Nie znaleziono użytkownika o id: " + hostUserId);
         }

         Users host = hostOpt.get();

         if(host.getZoomUserId() == null || host.getZoomAccessToken() == null){
                throw new IllegalArgumentException("Użytkownik nie jest połączony z Zoom");
         }

            ZoomMeetingRequest meetingRequest = ZoomMeetingRequest.builder()
                    .topic(title)
                    .type(2)
                    .startTime(startTime.atZone(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT))
                    .duration(durationMinutes)
                    .timezone("UTC")
                    .settings(ZoomMeetingSettings.builder()
                            .hostVideo(true)
                            .participantVideo(true)
                            .joinBeforeHost(false)
                            .muteUponEntry(true)
                            .autoRecording("cloud")
                            .waitingRoom(true)
                            .build())
                    .build();
         JsonNode response = zoomClient.createMeeting(objectMapper.convertValue(meetingRequest, Map.class), host.getZoomUserId());

         Meeting meeting = new Meeting();
         meeting.setTitle(title);
         meeting.setStartTime(startTime);
         meeting.setEndTime(startTime.plusMinutes(durationMinutes));
         meeting.setStatus(Status.PLANNED);
         meeting.setPlatform(Platform.ZOOM);
         meeting.setZoomHostId(host.getZoomUserId());
         meeting.setZoomJoinUrl(response.get("join_url").asText());

         return meetingRepository.save(meeting);
        } catch(Exception e){
            logger.error("Error creating zoom meeting", e);
            throw new RuntimeException("Błąd tworzenia spotkania");
        }
    }


    /**
     * METODY POMOCNICZE
     */
    private Optional<Meeting> findMeetingByZoomId(String zoomMeetingId){
        return meetingRepository.findAll().stream()
                .filter(m -> zoomMeetingId.equals(m.getZoomMeetingId()))
                .findFirst();
    }

    private String getFileExtension(String fileType){
        if(fileType.equals("MP4")) return ".mp4";
        if(fileType.equals("M4A")) return ".m4a";
        if(fileType.equals("VTT")) return ".vtt";
        if(fileType.equals("CHAT")) return ".txt";
        return "bin";
    }

    private Path downloadFile(String url, String fileName) throws IOException{
        Path filePath = Paths.get(RECORDING_DIRECTORY, fileName);

        byte[] fileContent = restTemplate.getForObject(URI.create(url), byte[].class);
        if(fileContent != null){
            Files.write(filePath, fileContent);
            return filePath;

        }
        throw new IOException("Error downloading file");
    }
}

