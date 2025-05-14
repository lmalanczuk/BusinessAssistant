package com.licencjat.BusinessAssistant.controller;

import com.licencjat.BusinessAssistant.model.SignalMessage;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class SignalController {

    @MessageMapping("/signal/{roomCode}")
    @SendTo("/topic/signal/{roomCode}")
    public SignalMessage handleSignal(@DestinationVariable String roomCode, SignalMessage message) {
        return message;
    }


}