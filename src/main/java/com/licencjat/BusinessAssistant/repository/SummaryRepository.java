package com.licencjat.BusinessAssistant.repository;

import com.licencjat.BusinessAssistant.entity.Summary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SummaryRepository extends JpaRepository<Summary, UUID> {
}
