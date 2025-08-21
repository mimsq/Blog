package com.xingmiao.blog.service;

public interface AccessTokenService {
    String generatePostAccessToken(Long postId, Long keyId, long ttlSeconds);
    boolean validatePostAccessToken(String token, Long postId);
    Long validateAndGetKeyId(String token, Long postId);
}


