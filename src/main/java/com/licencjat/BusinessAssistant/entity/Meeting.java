package com.licencjat.BusinessAssistant.entity;

import com.licencjat.BusinessAssistant.entity.enums.Platform;
import com.licencjat.BusinessAssistant.entity.enums.Status;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "Meeting")
@Data
@EqualsAndHashCode(exclude = "participants")
public class Meeting {

    @Id
    @GeneratedValue
    private UUID id;
    private String title;
    @Column(name = "start_time")
    private LocalDateTime startTime;
    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Enumerated(EnumType.STRING)
    private Platform platform;

    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "meeting_participants",
            joinColumns = @JoinColumn(name = "meeting_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    private Set<Users> participants = new HashSet<>();

   // Dodaj do Meeting.java
@Column(name = "daily_room_name")
private String dailyRoomName;

@Column(name = "daily_room_url")
private String dailyRoomUrl;

@Column(name = "daily_meeting_id")
private String dailyMeetingId;

@Column(name = "recording_url")
private String recordingUrl;

@Column(name = "actual_start_time")
private LocalDateTime actualStartTime;

@Column(name = "actual_end_time")
private LocalDateTime actualEndTime;

    // Metoda pomocnicza do zarządzania relacją
    public void addParticipant(Users user) {
        if (participants == null) {
            participants = new HashSet<>();
        }
        participants.add(user);
        // Zarządzamy relacją dwustronną, ale nie wywołujemy hashCode/equals
        if (user.getMeetings() == null) {
            user.setMeetings(new HashSet<>());
        }
        user.getMeetings().add(this);
    }
}