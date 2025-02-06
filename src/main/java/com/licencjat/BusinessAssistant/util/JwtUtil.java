//package com.licencjat.BusinessAssistant.util;
//import io.jsonwebtoken.SignatureAlgorithm;
//import io.jsonwebtoken.security.Keys;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.stereotype.Component;
//import org.springframework.stereotype.Component;
//import io.jsonwebtoken.Claims;
//import java.security.Key;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.Map;
//
//@Component
//public class JwtUtil {
//
//    //Generowanie klucza
//    private static final Key SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);
//
//    private static final long EXPIRATION_TIME = 86400000; // 1 dzień
//
//    //Generowanie tokena
//    public String generateToken(UserDetails userDetails) {
//        Map<String, Object> claims = new HashMap<>();
//        return createToken(claims, userDetails.getUsername());
//    }
//
//    //Tworzenie tokena
//    private String createToken(Map<String, Object> claims, String subject) {
//        return io.jsonwebtoken.Jwts.builder().setClaims(claims).setSubject(subject)
//                .setIssuedAt(new Date(System.currentTimeMillis()))
//                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
//                .signWith(SECRET_KEY).compact();
//    }
//
//    //Walidacja tokena
//    public Boolean validateToken(String token, UserDetails userDetails) {
//        final String username = extractUsername(token);
//        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
//    }
//
//    //Pobranie nazwy użytkownika z tokena
//    public String extractUsername(String token) {
//        return extractClaim(token, Claims::getSubject);
//    }
//    //Pobranie daty wygaśnięcia tokena
//    public Date extractExpiration(String token) {
//        return extractClaim(token, Claims::getExpiration);
//    }
//}
