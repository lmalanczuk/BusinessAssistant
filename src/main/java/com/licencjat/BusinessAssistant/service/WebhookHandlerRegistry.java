//package com.licencjat.BusinessAssistant.service;
//
//import com.licencjat.BusinessAssistant.webhook.WebhookHandler;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//import java.util.Map;
//
///**
// * Rejestr wszystkich handlerów webhooków
// */
//@Service
//public class WebhookHandlerRegistry {
//    private static final Logger logger = LoggerFactory.getLogger(WebhookHandlerRegistry.class);
//    private final List<WebhookHandler> handlers;
//
//    @Autowired
//    public WebhookHandlerRegistry(List<WebhookHandler> handlers) {
//        this.handlers = handlers;
//        logger.info("Zarejestrowano {} handlerów webhooków", handlers.size());
//        for (WebhookHandler handler : handlers) {
//            logger.debug("Zarejestrowano handler: {}", handler.getClass().getSimpleName());
//        }
//    }
//
//    /**
//     * Przetwarza webhook używając odpowiedniego handlera
//     */
//    public void processWebhook(String eventType, Map<String, Object> webhookData) {
//        logger.info("Przetwarzanie webhooka typu: {}", eventType);
//
//        boolean handled = false;
//        for (WebhookHandler handler : handlers) {
//            if (handler.canHandle(eventType)) {
//                logger.debug("Znaleziono odpowiedni handler: {}", handler.getClass().getSimpleName());
//                handler.handle(webhookData);
//                handled = true;
//                break;
//            }
//        }
//
//        if (!handled) {
//            logger.warn("Brak obsługi dla typu webhooka: {}", eventType);
//        }
//    }
//}