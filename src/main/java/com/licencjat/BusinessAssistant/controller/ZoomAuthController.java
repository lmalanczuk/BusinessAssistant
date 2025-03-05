package com.licencjat.BusinessAssistant.controller;

import com.licencjat.BusinessAssistant.config.ZoomConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Controller
@RequestMapping("/zoom")
public class ZoomAuthController {

    private final ZoomConfig zoomConfig;

    @Autowired
    public ZoomAuthController(ZoomConfig zoomConfig) {
        this.zoomConfig = zoomConfig;
    }

    @GetMapping("/authorize")
    public RedirectView authorizeZoom(@RequestParam("userId") UUID userId) {
        String authUrl = "https://zoom.us/oauth/authorize" +
                "?response_type=code" +
                "&client_id=" + zoomConfig.getClientId() +
                "&redirect_uri=" + URLEncoder.encode(zoomConfig.getRedirectUri(), StandardCharsets.UTF_8) +
                "&state=" + userId;

        return new RedirectView(authUrl);
    }

    @GetMapping("/auth-success")
    public String authSuccess() {
        return "zoom-auth-success";
    }
}