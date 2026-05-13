package com.habitus.api.service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.springframework.stereotype.Service;

import com.habitus.api.exception.UnauthorizedException;

@Service
public class TokenService {

    private static final String PREFIXO = "fake-token-";

    public String criarToken(Long userId) {
        String tokenDecodificado = PREFIXO + userId;
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenDecodificado.getBytes(StandardCharsets.UTF_8));
    }

    public Long lerIdUsuario(String token) {
        try {
            String tokenDecodificado = new String(Base64.getUrlDecoder().decode(token), StandardCharsets.UTF_8);
            if (!tokenDecodificado.startsWith(PREFIXO)) {
                throw new UnauthorizedException("Token inválido");
            }
            return Long.parseLong(tokenDecodificado.substring(PREFIXO.length()));
        } catch (IllegalArgumentException exception) {
            throw new UnauthorizedException("Token inválido");
        }
    }
}
