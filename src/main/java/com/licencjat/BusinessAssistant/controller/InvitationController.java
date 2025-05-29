package com.licencjat.BusinessAssistant.controller;

import com.licencjat.BusinessAssistant.model.Invitation;
import com.licencjat.BusinessAssistant.service.InvitationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/invitations")
public class InvitationController {

    private final InvitationService invitationService;

    public InvitationController(InvitationService invitationService) {
        this.invitationService = invitationService;
    }

    // GET /api/invitations
    @GetMapping
    public ResponseEntity<List<Invitation>> getInvitations() {
        List<Invitation> list = invitationService.getPendingInvitations();
        return ResponseEntity.ok(list);
    }
}
