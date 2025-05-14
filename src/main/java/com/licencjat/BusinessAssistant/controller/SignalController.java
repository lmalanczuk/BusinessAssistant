package com.licencjat.BusinessAssistant.controller;

import com.licencjat.BusinessAssistant.model.SignalMessage;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class SignalController {

    @MessageMapping("/signal")
    @SendTo("/topic/signal")
    public SignalMessage handleSignal(SignalMessage message) {
        return message;
    }

}