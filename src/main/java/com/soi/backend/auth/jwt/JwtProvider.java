package com.soi.backend.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtProvider {

    private static final String TOKEN_TYPE_CLAIM = "tokenType";
    private static final String SESSION_VERSION_CLAIM = "sessionVersion";
    private static final String ACCESS_TOKEN_TYPE = "ACCESS";
    private static final String REFRESH_TOKEN_TYPE = "REFRESH";

    private final SecretKey accessSecretKey;
    private final SecretKey refreshSecretKey;
    private final long accessExpirationMs;
    private final long refreshExpirationMs;

    public JwtProvider(
            @Value("${app.jwt.access-secret:${app.jwt.secret:}}") String rawAccessSecretKey,
            @Value("${app.jwt.refresh-secret:${app.jwt.access-secret:${app.jwt.secret:}}}") String rawRefreshSecretKey,
            @Value("${app.jwt.access-expiration-ms:${app.jwt.expiration-ms:1800000}}") long accessExpirationMs,
            @Value("${app.jwt.refresh-expiration-ms:1209600000}") long refreshExpirationMs
    ) {
        validateSecret("app.jwt.access-secret", rawAccessSecretKey);
        validateSecret("app.jwt.refresh-secret", rawRefreshSecretKey);

        this.accessSecretKey = Keys.hmacShaKeyFor(rawAccessSecretKey.getBytes(StandardCharsets.UTF_8));
        this.refreshSecretKey = Keys.hmacShaKeyFor(rawRefreshSecretKey.getBytes(StandardCharsets.UTF_8));
        this.accessExpirationMs = accessExpirationMs;
        this.refreshExpirationMs = refreshExpirationMs;
    }

    public String createToken(Long userId) {
        return createAccessToken(userId, 0);
    }

    public String createAccessToken(Long userId, Integer sessionVersion) {
        return createToken(userId, sessionVersion, ACCESS_TOKEN_TYPE, accessSecretKey, accessExpirationMs);
    }

    public String createRefreshToken(Long userId, Integer sessionVersion) {
        return createToken(userId, sessionVersion, REFRESH_TOKEN_TYPE, refreshSecretKey, refreshExpirationMs);
    }

    public Long getUserId(String token) {
        return getUserIdFromAccessToken(token);
    }

    public Long getUserIdFromAccessToken(String token) {
        return getUserId(token, ACCESS_TOKEN_TYPE, accessSecretKey);
    }

    public Long getUserIdFromRefreshToken(String token) {
        return getUserId(token, REFRESH_TOKEN_TYPE, refreshSecretKey);
    }

    public Integer getSessionVersionFromAccessToken(String token) {
        return getSessionVersion(token, ACCESS_TOKEN_TYPE, accessSecretKey);
    }

    public Integer getSessionVersionFromRefreshToken(String token) {
        return getSessionVersion(token, REFRESH_TOKEN_TYPE, refreshSecretKey);
    }

    public boolean validate(String token) {
        return validateAccessToken(token);
    }

    public boolean validateAccessToken(String token) {
        return validate(token, ACCESS_TOKEN_TYPE, accessSecretKey);
    }

    public boolean validateRefreshToken(String token) {
        return validate(token, REFRESH_TOKEN_TYPE, refreshSecretKey);
    }

    public long getAccessExpirationMs() {
        return accessExpirationMs;
    }

    public long getRefreshExpirationMs() {
        return refreshExpirationMs;
    }

    private String createToken(Long userId, Integer sessionVersion, String tokenType, SecretKey secretKey, long expirationMs) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim(TOKEN_TYPE_CLAIM, tokenType)
                .claim(SESSION_VERSION_CLAIM, sessionVersion)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    private Long getUserId(String token, String expectedTokenType, SecretKey secretKey) {
        Claims claims = parseClaims(token, secretKey);
        String tokenType = claims.get(TOKEN_TYPE_CLAIM, String.class);
        if (!expectedTokenType.equals(tokenType)) {
            throw new IllegalArgumentException("토큰 타입이 일치하지 않습니다.");
        }
        return Long.parseLong(claims.getSubject());
    }

    private Integer getSessionVersion(String token, String expectedTokenType, SecretKey secretKey) {
        Claims claims = parseClaims(token, secretKey);
        String tokenType = claims.get(TOKEN_TYPE_CLAIM, String.class);
        if (!expectedTokenType.equals(tokenType)) {
            throw new IllegalArgumentException("토큰 타입이 일치하지 않습니다.");
        }

        Number sessionVersion = claims.get(SESSION_VERSION_CLAIM, Number.class);
        if (sessionVersion == null) {
            throw new IllegalArgumentException("sessionVersion claim이 없습니다.");
        }

        return sessionVersion.intValue();
    }

    private boolean validate(String token, String expectedTokenType, SecretKey secretKey) {
        try {
            Claims claims = parseClaims(token, secretKey);
            return expectedTokenType.equals(claims.get(TOKEN_TYPE_CLAIM, String.class));
        } catch (Exception e) {
            return false;
        }
    }

    private Claims parseClaims(String token, SecretKey secretKey) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private void validateSecret(String propertyName, String rawSecretKey) {
        if (rawSecretKey == null || rawSecretKey.isBlank()) {
            throw new IllegalStateException(propertyName + " 설정이 필요합니다.");
        }

        if (rawSecretKey.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException(propertyName + " 는 최소 32바이트 이상이어야 합니다.");
        }
    }
}
