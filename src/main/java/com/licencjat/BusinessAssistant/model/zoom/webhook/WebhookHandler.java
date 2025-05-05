//package com.licencjat.BusinessAssistant.webhook;
//
//import java.util.Map;
//
///**
// * Ogólny interfejs dla obsługi webhooków
// */
//public interface WebhookHandler {
//    /**
//     * Sprawdza czy handler obsługuje dany typ zdarzenia
//     */
//    boolean canHandle(String eventType);
//
//    /**
//     * Przetwarza dane webhooka
//     */
//    void handle(Map<String, Object> webhookData);
//}