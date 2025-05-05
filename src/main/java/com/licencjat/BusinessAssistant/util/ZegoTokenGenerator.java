package com.licencjat.BusinessAssistant.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.licencjat.BusinessAssistant.config.ZegoConfig;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Random;

@Component
public class ZegoTokenGenerator {
    private static final Logger logger = LoggerFactory.getLogger(ZegoTokenGenerator.class);
    private static final String HMAC_SHA256 = "HmacSHA256";

    private final ZegoConfig zegoConfig;

    @Autowired
    public ZegoTokenGenerator(ZegoConfig zegoConfig) {
        this.zegoConfig = zegoConfig;
    }

    /**
     * Generuje token dla użytkownika ZEGOCLOUD
     *
     * @param userId ID użytkownika w aplikacji
     * @param roomId ID pokoju
     * @param privilege Poziom uprawnień (1=tylko subskrypcja, 2=publikowanie)
     * @param effectiveTimeInSeconds Czas ważności tokenu w sekundach
     * @return Wygenerowany token
     */
    public String generateToken(String userId, String roomId, int privilege, int effectiveTimeInSeconds) {
        try {
            long currentTimestamp = System.currentTimeMillis() / 1000;
            long expireTimestamp = currentTimestamp + effectiveTimeInSeconds;
            String nonce = generateRandomString(16);

            // Przygotowanie ciągu do podpisu
            String signContent = String.format("%d%s%s%d%s%d",
                    zegoConfig.getAppId(), userId, roomId, expireTimestamp, nonce, privilege);

            // Obliczenie HMAC-SHA256
            String signature = hmacSha256(signContent, zegoConfig.getServerSecret());

            // Utworzenie tokenu
            String token = String.format("%d:%s:%s:%d:%s:%d",
                    zegoConfig.getAppId(), userId, roomId, expireTimestamp, nonce, privilege);

            // Sklejenie tokenu i podpisu
            String finalToken = String.format("%s:%s", token, signature);

            // Kodowanie Base64
            return Base64.getEncoder().encodeToString(finalToken.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            logger.error("Błąd podczas generowania tokenu ZEGO: {}", e.getMessage());
            throw new RuntimeException("Nie można wygenerować tokenu ZEGO", e);
        }
    }

    private String hmacSha256(String data, String key) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac sha256Hmac = Mac.getInstance(HMAC_SHA256);
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), HMAC_SHA256);
        sha256Hmac.init(secretKey);
        byte[] hmacBytes = sha256Hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(hmacBytes);
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    private String generateRandomString(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder result = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            result.append(characters.charAt(random.nextInt(characters.length())));
        }
        return result.toString();
    }
}