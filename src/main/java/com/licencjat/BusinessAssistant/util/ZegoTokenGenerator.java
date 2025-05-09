package com.licencjat.BusinessAssistant.util;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.Random;

/**
 * Klasa do generowania tokenów uwierzytelniających dla ZegoCloud
 * Bazuje na oficjalnej implementacji TokenServerAssistant
 */
@Component
public class ZegoTokenGenerator {

    private static final String VERSION_FLAG = "04";
    private static final int IV_LENGTH = 16;
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";

    /**
     * Stałe określające uprawnienia
     */
    public static final String PRIVILEGE_LOGIN = "login_room";
    public static final String PRIVILEGE_PUBLISH = "publish";
    public static final int PRIVILEGE_ENABLE = 1;
    public static final int PRIVILEGE_DISABLE = 0;

    @Value("${zego.app-id}")
    private long appId;

    @Value("${zego.server-secret}")
    private String serverSecret;

    /**
     * Generuje token uwierzytelniający dla ZegoCloud
     * @param userId ID użytkownika
     * @param roomId ID pokoju, do którego użytkownik chce dołączyć
     * @param privileges Mapa uprawnień (klucz -> wartość)
     * @param effectiveTimeInSeconds Czas ważności tokenu w sekundach
     * @return Wygenerowany token
     */
    public String generateToken(String userId, String roomId, Map<String, Integer> privileges, int effectiveTimeInSeconds) {
        // Weryfikacja parametrów
        if (appId == 0) {
            throw new IllegalArgumentException("Invalid appId");
        }

        if (userId == null || userId.isEmpty() || userId.length() > 64) {
            throw new IllegalArgumentException("userId can't be empty and must be no more than 64 characters");
        }

        if (serverSecret == null || serverSecret.length() != 32) {
            throw new IllegalArgumentException("Secret must be 32 characters");
        }

        if (effectiveTimeInSeconds <= 0) {
            throw new IllegalArgumentException("effectiveTimeInSeconds must be greater than 0");
        }

        try {
            // Generowanie losowego IV
            byte[] ivBytes = new byte[IV_LENGTH];
            new SecureRandom().nextBytes(ivBytes);

            // Przygotowanie payloadu
            JSONObject payloadObj = new JSONObject();
            if (roomId != null && !roomId.isEmpty()) {
                payloadObj.put("room_id", roomId);
            }

            // Dodanie uprawnień
            if (privileges != null && !privileges.isEmpty()) {
                JSONObject privilegeObj = new JSONObject();
                for (Map.Entry<String, Integer> entry : privileges.entrySet()) {
                    privilegeObj.put(entry.getKey(), entry.getValue());
                }
                payloadObj.put("privilege", privilegeObj);
            }

            // Przygotowanie kompletnego obiektu JSON
            JSONObject contentJson = new JSONObject();
            contentJson.put("app_id", appId);
            contentJson.put("user_id", userId);

            long nowTime = System.currentTimeMillis() / 1000;
            long expireTime = nowTime + effectiveTimeInSeconds;
            contentJson.put("ctime", nowTime);
            contentJson.put("expire", expireTime);
            contentJson.put("nonce", new Random().nextInt());

            // Dodanie payloadu do głównego obiektu JSON
            if (!payloadObj.isEmpty()) {
                contentJson.put("payload", payloadObj.toString());
            } else {
                contentJson.put("payload", "");
            }

            String content = contentJson.toString();

            // Szyfrowanie zawartości
            byte[] contentBytes = encrypt(content.getBytes(StandardCharsets.UTF_8),
                                         serverSecret.getBytes(StandardCharsets.UTF_8),
                                         ivBytes);

            // Tworzenie bufora z tokenem
            ByteBuffer buffer = ByteBuffer.allocate(8 + 2 + ivBytes.length + 2 + contentBytes.length);
            buffer.order(ByteOrder.BIG_ENDIAN);

            buffer.putLong(expireTime);
            packBytes(ivBytes, buffer);
            packBytes(contentBytes, buffer);

            // Kodowanie do Base64
            return VERSION_FLAG + Base64.getEncoder().encodeToString(buffer.array());

        } catch (Exception e) {
            throw new RuntimeException("Błąd podczas generowania tokenu: " + e.getMessage(), e);
        }
    }

    private static byte[] encrypt(byte[] content, byte[] secretKey, byte[] ivBytes) throws Exception {
        if (secretKey == null || secretKey.length != 32) {
            throw new IllegalArgumentException("Klucz tajny musi mieć 32 bajty");
        }

        if (ivBytes == null || ivBytes.length != 16) {
            throw new IllegalArgumentException("IV musi mieć 16 bajtów");
        }

        if (content == null) {
            content = new byte[0];
        }

        SecretKeySpec key = new SecretKeySpec(secretKey, "AES");
        IvParameterSpec iv = new IvParameterSpec(ivBytes);

        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, key, iv);

        return cipher.doFinal(content);
    }

    private static void packBytes(byte[] buffer, ByteBuffer target) {
        target.putShort((short) buffer.length);
        target.put(buffer);
    }
}