package com.tourist.Yatra_secure.Security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import io.jsonwebtoken.Claims;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class JwtService {

    private static final long TOKEN_TTL_MILLIS = 86400000L;

    @Value("${jwt.secret}")
    private String secret;

    // Secret key generate karna
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // JWT token generate karna
    public String generateToken(String email) {
        return generateToken(email, "ROLE_TOURIST");
    }

    public String generateToken(String email, String role) {

        long currentTime = System.currentTimeMillis();

        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .issuedAt(new Date(currentTime))
                .expiration(new Date(currentTime + TOKEN_TTL_MILLIS))
                .signWith(getSigningKey())
                .compact();
    }

    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    public boolean isTokenValid(String token, String email) {
        Claims claims = extractAllClaims(token);
        return email.equals(claims.getSubject())
                && claims.getExpiration() != null
                && claims.getExpiration().after(new Date());
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public long getExpirationSeconds() { return TOKEN_TTL_MILLIS / 1000; }
}

