//package com.licencjat.BusinessAssistant.security;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//
//import javax.crypto.Mac;
//import javax.crypto.spec.SecretKeySpec;
//import java.nio.charset.StandardCharsets;
//import java.util.Base64;
//
//@Component
//public class ZoomWebhookValidator {
//    private static final Logger logger = LoggerFactory.getLogger(ZoomWebhookValidator.class);
//
//    @Value("${zoom.verification-token}")
//    private String verificationToken;
//
//    @Value("${zoom.webhook-secret:}")
//    private String webhookSecret;
//
//    /**
//     * Validates if a webhook is genuinely from Zoom using token verification
//     * @param requestToken Token received in the webhook payload
//     * @return true if valid, false otherwise
//     */
//    public boolean isValidToken(String requestToken) {
//        if (requestToken == null || requestToken.isEmpty()) {
//            logger.warn("Received webhook with empty token");
//            return false;
//        }
//
//        boolean isValid = verificationToken.equals(requestToken);
//        if (!isValid) {
//            logger.warn("Webhook token validation failed");
//        }
//        return isValid;
//    }
//
//    /**
//     * Validates webhook using the newer v2 signature validation
//     * @param payload Webhook payload as string
//     * @param timestamp Timestamp header value
//     * @param signature Signature header value
//     * @return true if valid, false otherwise
//     */
//    public boolean isValidSignature(String payload, String timestamp, String signature) {
//        // If webhook secret is not configured, we can't validate the signature
//        if (webhookSecret == null || webhookSecret.isEmpty()) {
//            logger.warn("Webhook secret not configured, skipping signature validation");
//            return true;
//        }
//
//        if (timestamp == null || signature == null) {
//            logger.warn("Missing timestamp or signature headers for webhook validation");
//            return false;
//        }
//
//        try {
//            Mac hmac = Mac.getInstance("HmacSHA256");
//            SecretKeySpec secretKey = new SecretKeySpec(
//                    webhookSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
//            hmac.init(secretKey);
//
//            // Verify v2 signature format (hash of timestamp + payload)
//            String message = timestamp + payload;
//            byte[] hash = hmac.doFinal(message.getBytes(StandardCharsets.UTF_8));
//            String hashBase64 = Base64.getEncoder().encodeToString(hash);
//
//            boolean isValid = hashBase64.equals(signature);
//            if (!isValid) {
//                logger.warn("Webhook signature validation failed");
//            }
//            return isValid;
//        } catch (Exception e) {
//            logger.error("Error validating webhook signature", e);
//            return false;
//        }
//    }
//}