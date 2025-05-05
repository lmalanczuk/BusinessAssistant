//package com.licencjat.BusinessAssistant.platform.zoom;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.licencjat.BusinessAssistant.client.zoom.ZoomMeetingClient;
//import com.licencjat.BusinessAssistant.client.zoom.ZoomRecordingClient;
//import com.licencjat.BusinessAssistant.model.zoom.ZoomMeetingRequest;
//import com.licencjat.BusinessAssistant.model.zoom.ZoomMeetingSettings;
//import com.licencjat.BusinessAssistant.platform.MeetingPlatformClient;
//import com.licencjat.BusinessAssistant.exception.ZoomApiException;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.HttpStatusCode;
//import org.springframework.web.client.HttpClientErrorException;
//import org.springframework.web.client.HttpServerErrorException;
//
//import java.time.LocalDateTime;
//import java.time.ZoneOffset;
//import java.time.format.DateTimeFormatter;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//
//public class ZoomPlatformClient implements MeetingPlatformClient {
//    private static final Logger logger = LoggerFactory.getLogger(ZoomPlatformClient.class);
//    private static final int MAX_RETRY_ATTEMPTS = 3;
//    private static final long RETRY_DELAY_MS = 1000; // 1 second delay between retries
//
//    private final ZoomMeetingClient meetingClient;
//    private final ZoomRecordingClient recordingClient;
//    private final ObjectMapper objectMapper = new ObjectMapper();
//
//    public ZoomPlatformClient(ZoomMeetingClient meetingClient, ZoomRecordingClient recordingClient) {
//        this.meetingClient = meetingClient;
//        this.recordingClient = recordingClient;
//    }
//
//    @Override
//    public JsonNode createMeeting(String title, LocalDateTime startTime, int durationMinutes,
//                                 String userId, Map<String, Object> options) {
//        try {
//            // Konwersja parametrów na format specyficzny dla Zoom
//            ZoomMeetingSettings.Builder settingsBuilder = ZoomMeetingSettings.builder()
//                    .hostVideo(true)
//                    .participantVideo(true)
//                    .joinBeforeHost(false)
//                    .muteUponEntry(true)
//                    .autoRecording("cloud")
//                    .waitingRoom(true);
//
//            // Dodanie opcjonalnych ustawień, jeśli zostały przekazane
//            if (options != null) {
//                if (options.containsKey("hostVideo")) {
//                    settingsBuilder.hostVideo((Boolean) options.get("hostVideo"));
//                }
//                if (options.containsKey("participantVideo")) {
//                    settingsBuilder.participantVideo((Boolean) options.get("participantVideo"));
//                }
//                if (options.containsKey("joinBeforeHost")) {
//                    settingsBuilder.joinBeforeHost((Boolean) options.get("joinBeforeHost"));
//                }
//                if (options.containsKey("muteUponEntry")) {
//                    settingsBuilder.muteUponEntry((Boolean) options.get("muteUponEntry"));
//                }
//                if (options.containsKey("autoRecording")) {
//                    settingsBuilder.autoRecording((String) options.get("autoRecording"));
//                }
//                if (options.containsKey("waitingRoom")) {
//                    settingsBuilder.waitingRoom((Boolean) options.get("waitingRoom"));
//                }
//            }
//
//            ZoomMeetingRequest meetingRequest = ZoomMeetingRequest.builder()
//                    .topic(title)
//                    .type(2) // Scheduled meeting
//                    .startTime(startTime.atZone(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT))
//                    .duration(durationMinutes)
//                    .timezone("UTC")
//                    .settings(settingsBuilder.build())
//                    .build();
//
//            Map<String, Object> meetingDetails = objectMapper.convertValue(meetingRequest, Map.class);
//
//            return executeWithRetry(() -> meetingClient.createMeeting(meetingDetails, userId),
//                    "create meeting for user " + userId);
//        } catch (Exception e) {
//            logger.error("Error creating Zoom meeting: {}", e.getMessage());
//            throw new ZoomApiException("Error creating Zoom meeting", e);
//        }
//    }
//
//    @Override
//    public JsonNode getMeeting(String meetingId) {
//        return executeWithRetry(() -> meetingClient.getMeeting(meetingId),
//                "get meeting " + meetingId);
//    }
//
//    @Override
//    public JsonNode updateMeeting(String meetingId, Map<String, Object> updateData) {
//        return executeWithRetry(() -> meetingClient.updateMeeting(meetingId, updateData),
//                "update meeting " + meetingId);
//    }
//
//    @Override
//    public JsonNode startMeeting(String meetingId) {
//        return executeWithRetry(() -> meetingClient.startMeeting(meetingId),
//                "start meeting " + meetingId);
//    }
//
//    @Override
//    public JsonNode endMeeting(String meetingId) {
//        return executeWithRetry(() -> meetingClient.endMeeting(meetingId),
//                "end meeting " + meetingId);
//    }
//
//    @Override
//    public JsonNode getMeetingParticipants(String meetingId) {
//        return executeWithRetry(() -> meetingClient.getMeetingParticipants(meetingId),
//                "get participants for meeting " + meetingId);
//    }
//
//    @Override
//    public JsonNode inviteToMeeting(String meetingId, List<String> emails) {
//        return executeWithRetry(() -> meetingClient.inviteToMeeting(meetingId, emails),
//                "invite participants to meeting " + meetingId);
//    }
//
//    @Override
//    public JsonNode getMeetingRecordings(String meetingId) {
//        return executeWithRetry(() -> recordingClient.getMeetingRecordings(meetingId),
//                "get recordings for meeting " + meetingId);
//    }
//
//    /**
//     * Helper interface for retryable operations
//     */
//    private interface ZoomOperation {
//        JsonNode execute();
//    }
//
//    /**
//     * Execute a Zoom API operation with retry logic
//     */
//    private JsonNode executeWithRetry(ZoomOperation operation, String operationDesc) {
//    int attempts = 0;
//    HttpClientErrorException lastClientError = null;
//    HttpServerErrorException lastServerError = null;
//    Exception lastException = null;
//
//    while (attempts < MAX_RETRY_ATTEMPTS) {
//        try {
//            return operation.execute();
//        } catch (HttpClientErrorException e) {
//            lastClientError = e;
//            HttpStatusCode statusCode = e.getStatusCode();
//
//            // Don't retry for certain client errors
//            if (statusCode.equals(HttpStatus.UNAUTHORIZED) ||
//                statusCode.equals(HttpStatus.FORBIDDEN) ||
//                statusCode.equals(HttpStatus.NOT_FOUND)) {
//                logger.error("Non-retryable client error for operation '{}': {} - {}",
//                        operationDesc, statusCode, e.getMessage());
//                handleSpecificZoomError(e);
//                break;
//            }
//
//            // For rate limiting, wait longer
//            if (statusCode.equals(HttpStatus.TOO_MANY_REQUESTS)) {
//                long retryAfter = e.getResponseHeaders() != null &&
//                                 e.getResponseHeaders().getFirst("Retry-After") != null ?
//                        Long.parseLong(e.getResponseHeaders().getFirst("Retry-After")) * 1000 :
//                        RETRY_DELAY_MS * (attempts + 1);
//
//                logger.warn("Rate limit hit for operation '{}', waiting {}ms before retry",
//                        operationDesc, retryAfter);
//                sleep(retryAfter);
//            } else {
//                logger.warn("Client error for operation '{}': {} - {}, attempt {}/{}",
//                        operationDesc, statusCode, e.getMessage(), attempts+1, MAX_RETRY_ATTEMPTS);
//                sleep(RETRY_DELAY_MS * (attempts + 1));
//            }
//        } catch (HttpServerErrorException e) {
//            lastServerError = e;
//            logger.warn("Server error for operation '{}': {} - {}, attempt {}/{}",
//                    operationDesc, e.getStatusCode(), e.getMessage(), attempts+1, MAX_RETRY_ATTEMPTS);
//            sleep(RETRY_DELAY_MS * (attempts + 1));
//        } catch (Exception e) {
//            lastException = e;
//            logger.warn("Unexpected error for operation '{}': {}, attempt {}/{}",
//                    operationDesc, e.getMessage(), attempts+1, MAX_RETRY_ATTEMPTS);
//            sleep(RETRY_DELAY_MS * (attempts + 1));
//        }
//
//        attempts++;
//    }
//
//    // If we get here, all retries failed
//    if (lastClientError != null) {
//        handleSpecificZoomError(lastClientError);
//    } else if (lastServerError != null) {
//        throw new ZoomApiException("Zoom API server error after " + MAX_RETRY_ATTEMPTS +
//                " attempts for operation '" + operationDesc + "'", lastServerError);
//    } else if (lastException != null) {
//        throw new ZoomApiException("Failed to " + operationDesc + " after " +
//                MAX_RETRY_ATTEMPTS + " attempts", lastException);
//    } else {
//        // This shouldn't happen, but just in case
//        throw new ZoomApiException("Failed to " + operationDesc + " after " +
//                MAX_RETRY_ATTEMPTS + " attempts for unknown reason");
//    }
//
//    // This line is unreachable, but compiler doesn't know that
//    return null;
//}
//
//    /**
//     * Handle specific error responses from Zoom API
//     */
//    private void handleSpecificZoomError(HttpClientErrorException e) {
//    try {
//        JsonNode errorJson = objectMapper.readTree(e.getResponseBodyAsString());
//        int errorCode = errorJson.has("code") ? errorJson.get("code").asInt() : 0;
//        String errorMessage = errorJson.has("message") ? errorJson.get("message").asText() : e.getMessage();
//
//        switch (errorCode) {
//            // Błędy spotkań
//            case 124:
//                throw new ZoomApiException("Meeting not found: " + errorMessage);
//            case 200:
//                throw new ZoomApiException("Permission denied: " + errorMessage);
//            case 300:
//                throw new ZoomApiException("Invalid parameter: " + errorMessage);
//            case 3000:
//                throw new ZoomApiException("Cannot access webinar: " + errorMessage);
//            case 3001:
//                throw new ZoomApiException("Meeting is over: " + errorMessage);
//
//            // Błędy OAuth
//            case 1000:
//                throw new ZoomApiException("OAuth error: " + errorMessage);
//            case 1001:
//                throw new ZoomApiException("Access token invalid or expired: " + errorMessage);
//            case 1002:
//                throw new ZoomApiException("Invalid response: " + errorMessage);
//            case 1003:
//                throw new ZoomApiException("Invalid authorization code: " + errorMessage);
//            case 1004:
//                throw new ZoomApiException("Access token expired: " + errorMessage);
//            case 1010:
//                throw new ZoomApiException("Invalid refresh token: " + errorMessage);
//
//            // Rate limiting
//            case 429:
//                throw new ZoomApiException("Rate limit exceeded: " + errorMessage);
//
//            // Błędy dostępu
//            case 400:
//                throw new ZoomApiException("Bad request: " + errorMessage);
//            case 401:
//                throw new ZoomApiException("Unauthorized: " + errorMessage);
//            case 403:
//                throw new ZoomApiException("Forbidden: " + errorMessage);
//            case 404:
//                throw new ZoomApiException("Resource not found: " + errorMessage);
//
//            default:
//                throw new ZoomApiException("Zoom API error (" + errorCode + "): " + errorMessage, e);
//        }
//    } catch (ZoomApiException ze) {
//        throw ze;
//    } catch (Exception jsonError) {
//        // If we can't parse the error JSON, just throw with the original message
//        throw new ZoomApiException("Zoom API error: " + e.getMessage(), e);
//    }
//}
//
//    private void sleep(long ms) {
//        try {
//            Thread.sleep(ms);
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//            logger.warn("Sleep interrupted during retry delay");
//        }
//    }
//
//    @Override
//    public JsonNode getUserRecordings(String userId, String from, String to) {
//    return executeWithRetry(() -> recordingClient.getUserRecordings(userId, from, to),
//            "get recordings for user " + userId);
//}
//}