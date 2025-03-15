package com.licencjat.BusinessAssistant.entity;

import com.licencjat.BusinessAssistant.entity.enums.Platform;
import com.licencjat.BusinessAssistant.entity.enums.Status;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "Meeting")
@Data
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
    @ManyToMany
    @JoinTable(
            name = "meeting_participants",
            joinColumns = @JoinColumn(name = "meeting_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    private Set<Users> participants;

    @Column(name = "zoom_meeting_id")
    private String zoomMeetingId;

    @Column(name = "zoom_host_id")
    private String zoomHostId;

    @Column(name = "zoom_join_url")
    private String zoomJoinUrl;

    @Column(name = "zoom_recording_url")
    private String zoomRecordingUrl;

}
