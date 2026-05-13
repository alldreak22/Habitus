package com.habitus.api.service;

import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.habitus.api.entity.User;
import com.habitus.api.exception.UnauthorizedException;
import com.habitus.api.repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final TokenService tokenService;
    private final UserRepository userRepository;

    public User obterUsuarioAtual() {
        HttpServletRequest request = requisicaoAtual();
        String autorizacao = request.getHeader("Authorization");
        if (autorizacao == null || !autorizacao.startsWith("Bearer ")) {
            throw new UnauthorizedException("Token bearer ausente");
        }

        Long userId = tokenService.lerIdUsuario(autorizacao.substring("Bearer ".length()));
        return userRepository.findById(userId)
            .orElseThrow(() -> new UnauthorizedException("Usuário não encontrado para o token"));
    }

    private HttpServletRequest requisicaoAtual() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            return attributes.getRequest();
        }
        throw new UnauthorizedException("Nenhuma requisição ativa");
    }
}
