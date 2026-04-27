package com.fishcam.infrastructure.security;

import com.fishcam.domain.user.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String SECRET_KEY;

    @Value("${jwt.expiration}")
    private Long EXPIRATION_TIME;

    /**
     * Génère un token JWT pour un utilisateur
     */
    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("phone", user.getPhone());  // ← PHONE au lieu d'email
        claims.put("role", user.getRole().name());
        claims.put("firstName", user.getFirstName());
        claims.put("lastName", user.getLastName());
        claims.put("poissonnerieId", user.getDefaultPoissonnerie() != null
                ? user.getDefaultPoissonnerie().getId()
                : null);

        String token = Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getPhone())  // ← PHONE au lieu d'email
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();

        log.info("Token généré pour l'utilisateur: {} ({})", user.getPhone(), user.getRole());
        return token;
    }

    /**
     * Extrait le téléphone (username) du token
     */
    public String extractPhone(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return claims.getSubject();
        } catch (Exception e) {
            log.error("Erreur extraction phone du token", e);
            return null;
        }
    }

    /**
     * Extrait l'ID utilisateur du token
     */
    public Long extractUserId(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return claims.get("userId", Long.class);
        } catch (Exception e) {
            log.error("Erreur extraction userId du token", e);
            return null;
        }
    }

    /**
     * Valide le token
     */
    public boolean validateToken(String token, User user) {
        try {
            final String phone = extractPhone(token);
            boolean isValid = phone.equals(user.getPhone()) && !isTokenExpired(token);

            if (isValid) {
                log.debug("Token valide pour: {}", phone);
            } else {
                log.warn("Token invalide ou expiré pour: {}", phone);
            }

            return isValid;
        } catch (Exception e) {
            log.error("Erreur validation token", e);
            return false;
        }
    }

    /**
     * Vérifie si le token est expiré
     */
    private boolean isTokenExpired(String token) {
        try {
            Date expiration = extractAllClaims(token).getExpiration();
            return expiration.before(new Date());
        } catch (ExpiredJwtException e) {
            log.warn("Token expiré");
            return true;
        }
    }

    /**
     * Extrait toutes les claims du token
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Récupère la clé de signature
     */
    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}