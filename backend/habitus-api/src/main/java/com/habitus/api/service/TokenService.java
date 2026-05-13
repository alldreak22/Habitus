package com.habitus.api.service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.springframework.stereotype.Service;

import com.habitus.api.exception.UnauthorizedException;

@Service
public class TokenService {

    private static final String PREFIX = "fake-token-";

    public String createToken(Long userId) {
        String rawToken = PREFIX + userId;
        return Base64.getUrlEncoder().withoutPadding().encodeToString(rawToken.getBytes(StandardCharsets.UTF_8));
    }

    public Long readUserId(String token) {
        try {
            String rawToken = new String(Base64.getUrlDecoder().decode(token), StandardCharsets.UTF_8);
            if (!rawToken.startsWith(PREFIX)) {
                throw new UnauthorizedException("Invalid token");
            }
            return Long.parseLong(rawToken.substring(PREFIX.length()));
        } catch (IllegalArgumentException exception) {
            throw new UnauthorizedException("Invalid token");
        }
    }
}
