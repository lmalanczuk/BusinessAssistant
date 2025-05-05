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

    @Column(name = "zego_room_id")
    private String zegoRoomId;

    @Column(name = "zego_stream_id")
    private String zegoStreamId;

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