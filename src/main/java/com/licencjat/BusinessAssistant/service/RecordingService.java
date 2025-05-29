package com.licencjat.BusinessAssistant.service;

import com.licencjat.BusinessAssistant.model.Recording;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class RecordingService {
    public List<Recording> getRecentRecordings() {
        // TODO: implement real data fetch
        return Collections.emptyList();
    }
}
