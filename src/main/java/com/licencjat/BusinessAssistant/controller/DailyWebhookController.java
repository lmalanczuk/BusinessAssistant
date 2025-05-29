package com.licencjat.BusinessAssistant.controller;

import com.licencjat.BusinessAssistant.service.DailyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/webhooks/daily")
public class DailyWebhookController {
    private static final Logger logger = LoggerFactory.getLogger(DailyWebhookController.class);

    private final DailyService dailyService;

    @Autowired
    public DailyWebhookController(DailyService dailyService) {
        this.dailyService = dailyService;
    }

    @PostMapping
    public ResponseEntity<String> handleWebhook(@RequestBody Map<String, Object> payload) {
        try {
            dailyService.handleWebhook(payload);
            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            logger.error("Błąd przetwarzania webhooka Daily", e);
            return ResponseEntity.internalServerError().body("Error");
        }
    }
}
