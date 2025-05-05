package com.licencjat.BusinessAssistant.service;

import com.licencjat.BusinessAssistant.entity.Meeting;
import com.licencjat.BusinessAssistant.entity.Users;
import com.licencjat.BusinessAssistant.entity.enums.Role;
import com.licencjat.BusinessAssistant.entity.enums.Status;
import com.licencjat.BusinessAssistant.exception.ResourceNotFoundException;
import com.licencjat.BusinessAssistant.repository.MeetingRepository;
import com.licencjat.BusinessAssistant.repository.UserRepository;
import com.licencjat.BusinessAssistant.util.ZegoTokenGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ZegoServiceTest {

    @Mock
    private ZegoTokenGenerator tokenGenerator;

    @Mock
    private MeetingRepository meetingRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ZegoService zegoService;

    private UUID userId;
    private Users testUser;
    private Meeting testMeeting;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        userId = UUID.randomUUID();
        testUser = new Users();
        testUser.setId(userId);
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setEmail("test@example.com");
        testUser.setUsername("testuser");
        testUser.setRole(Role.USER);

        testMeeting = new Meeting();
        testMeeting.setId(UUID.randomUUID());
        testMeeting.setTitle("Test Meeting");
        testMeeting.setStartTime(LocalDateTime.now().plusHours(1));
        testMeeting.setEndTime(LocalDateTime.now().plusHours(2));
        testMeeting.setStatus(Status.PLANNED);
        testMeeting.setZegoRoomId("test-room-id");
        testMeeting.setParticipants(new HashSet<>());
    }

    @Test
    void generateToken_ShouldReturnToken() {
        // Given
        String roomId = "test-room-id";
        int expireTime = 3600;
        String expectedToken = "test-token";

        when(tokenGenerator.generateToken(anyString(), anyString(), anyInt(), anyInt()))
                .thenReturn(expectedToken);

        // When
        String token = zegoService.generateToken(userId.toString(), roomId, expireTime);

        // Then
        assertEquals(expectedToken, token);
        verify(tokenGenerator).generateToken(userId.toString(), roomId, 2, expireTime);
    }

    @Test
    void createMeeting_ShouldCreateMeeting() {
        // Given
        String title = "Test Meeting";
        LocalDateTime startTime = LocalDateTime.now().plusHours(1);
        int durationMinutes = 60;

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(meetingRepository.save(any(Meeting.class))).thenReturn(testMeeting);

        // When
        Meeting result = zegoService.createMeeting(title, startTime, durationMinutes, userId);

        // Then
        assertNotNull(result);
        assertEquals(testMeeting.getId(), result.getId());
        verify(userRepository).findById(userId);
        verify(meetingRepository).save(any(Meeting.class));
    }

    @Test
    void createMeeting_ShouldThrowException_WhenUserNotFound() {
        // Given
        String title = "Test Meeting";
        LocalDateTime startTime = LocalDateTime.now().plusHours(1);
        int durationMinutes = 60;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            zegoService.createMeeting(title, startTime, durationMinutes, userId);
        });
        verify(userRepository).findById(userId);
        verify(meetingRepository, never()).save(any(Meeting.class));
    }

    @Test
    void startMeeting_ShouldUpdateStatus() {
        // Given
        UUID meetingId = testMeeting.getId();
        when(meetingRepository.findById(meetingId)).thenReturn(Optional.of(testMeeting));
        when(meetingRepository.save(any(Meeting.class))).thenReturn(testMeeting);

        // When
        Meeting result = zegoService.startMeeting(meetingId);

        // Then
        assertNotNull(result);
        assertEquals(Status.ONGOING, result.getStatus());
        verify(meetingRepository).findById(meetingId);
        verify(meetingRepository).save(testMeeting);
    }

    @Test
    void endMeeting_ShouldUpdateStatus() {
        // Given
        UUID meetingId = testMeeting.getId();
        when(meetingRepository.findById(meetingId)).thenReturn(Optional.of(testMeeting));
        when(meetingRepository.save(any(Meeting.class))).thenReturn(testMeeting);

        // When
        Meeting result = zegoService.endMeeting(meetingId);

        // Then
        assertNotNull(result);
        assertEquals(Status.COMPLETED, result.getStatus());
        verify(meetingRepository).findById(meetingId);
        verify(meetingRepository).save(testMeeting);
    }
}