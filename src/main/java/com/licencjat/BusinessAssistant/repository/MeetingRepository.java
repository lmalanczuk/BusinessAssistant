package com.licencjat.BusinessAssistant.repository;

import com.licencjat.BusinessAssistant.entity.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface MeetingRepository extends JpaRepository<Meeting, UUID> {

    /**
     * Find meeting by its Zoom ID
     */
    @Query("SELECT m FROM Meeting m WHERE m.zegoRoomId = :zegoRoomId")
    Optional<Meeting> findByZegoRoomId(@Param("zegoRoomId") String zegoRoomId);
}