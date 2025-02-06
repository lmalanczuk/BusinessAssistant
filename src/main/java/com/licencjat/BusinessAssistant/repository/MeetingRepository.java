package com.licencjat.BusinessAssistant.repository;

import com.licencjat.BusinessAssistant.entity.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MeetingRepository extends JpaRepository<Meeting, UUID> {
}
