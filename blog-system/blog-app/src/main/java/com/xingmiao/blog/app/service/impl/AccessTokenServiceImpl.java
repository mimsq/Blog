package com.xingmiao.blog.app.service.impl;

import com.xingmiao.blog.app.service.AccessTokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class AccessTokenServiceImpl implements AccessTokenService {
    
    @Value("${blog.security.jwt.secret:defaultSecretKeyForDevelopmentOnly}")
    private String jwtSecret;
    
    @Value("${blog.security.jwt.default-ttl:86400}")
    private long defaultTtl;
    
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }
    
    @Override
    public String generatePostAccessToken(Long postId, Long keyId, long ttlSeconds) {
        if (ttlSeconds <= 0) {
            ttlSeconds = defaultTtl;
        }
        
        Map<String, Object> claims = new HashMap<>();
        claims.put("postId", postId);
        claims.put("keyId", keyId);
        
        return Jwts.builder()
                .claims(claims)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + ttlSeconds * 1000))
                .signWith(getSigningKey())
                .compact();
    }

    @Override
    public boolean validatePostAccessToken(String token, Long postId) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            
            if (claims.getExpiration().before(new Date())) {
                return false;
            }
            
            Long tokenPostId = claims.get("postId", Long.class);
            return postId.equals(tokenPostId);
            
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public Long validateAndGetKeyId(String token, Long postId) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            
            if (claims.getExpiration().before(new Date())) {
                return null;
            }
            
            Long tokenPostId = claims.get("postId", Long.class);
            if (!postId.equals(tokenPostId)) {
                return null;
            }
            
            return claims.get("keyId", Long.class);
            
        } catch (Exception e) {
            return null;
        }
    }
}
