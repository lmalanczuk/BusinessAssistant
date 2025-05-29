package com.licencjat.BusinessAssistant.repository;

import com.licencjat.BusinessAssistant.entity.Meeting;
import com.licencjat.BusinessAssistant.entity.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MeetingRepository extends JpaRepository<Meeting, UUID> {
    Optional<Meeting> findByStatusAndStartTimeBeforeAndEndTimeAfter(
        Status status, LocalDateTime before, LocalDateTime after
    );
    List<Meeting> findByStartTimeAfterOrderByStartTimeAsc(LocalDateTime now);
    Optional<Meeting> findByDailyRoomName(String roomName);
}
