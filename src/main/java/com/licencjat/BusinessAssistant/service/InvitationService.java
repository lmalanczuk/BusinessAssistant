package com.licencjat.BusinessAssistant.service;

import com.licencjat.BusinessAssistant.model.Invitation;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class InvitationService {
    public List<Invitation> getPendingInvitations() {
        // TODO: implement real data fetch
        return Collections.emptyList();
    }
}
