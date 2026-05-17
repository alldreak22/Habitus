package com.habitus.api.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.habitus.api.dto.request.LoginRequest;
import com.habitus.api.dto.request.RegisterRequest;
import com.habitus.api.dto.response.AuthResponse;
import com.habitus.api.dto.response.UserResponse;
import com.habitus.api.entity.User;
import com.habitus.api.exception.ApiException;
import com.habitus.api.exception.UnauthorizedException;
import com.habitus.api.mapper.ApiMapper;
import com.habitus.api.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final ApiMapper mapper;

    @Transactional
    public AuthResponse registrar(RegisterRequest requisicao) {
        String email = normalizarEmail(requisicao.email());
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ApiException(HttpStatus.CONFLICT, "E-mail j\u00e1 cadastrado");
        }

        User user = new User();
        user.setName(requisicao.name().trim());
        user.setEmail(email);
        user.setNick(gerarNickUnico(email));
        user.setPasswordHash(passwordEncoder.encode(requisicao.password()));
        User savedUser = userRepository.save(user);

        return respostaAutenticacao(savedUser);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest requisicao) {
        User user = userRepository.findByEmailIgnoreCase(normalizarEmail(requisicao.email()))
            .orElseThrow(() -> new UnauthorizedException("E-mail ou senha inv\u00e1lidos"));

        if (!passwordEncoder.matches(requisicao.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("E-mail ou senha inv\u00e1lidos");
        }

        return respostaAutenticacao(user);
    }

    @Transactional(readOnly = true)
    public UserResponse buscarUsuarioAtual(User user) {
        return mapper.toUserResponse(user);
    }

    private AuthResponse respostaAutenticacao(User user) {
        return new AuthResponse(mapper.toUserResponse(user), tokenService.criarToken(user.getId()), "Bearer");
    }

    private String normalizarEmail(String email) {
        return email.trim().toLowerCase();
    }

    private String gerarNickUnico(String email) {
        String base = email.substring(0, email.indexOf('@')).replaceAll("[^a-zA-Z0-9._-]", "").toLowerCase();
        if (base.isBlank()) {
            base = "user";
        }

        String candidate = base;
        int suffix = 1;
        while (userRepository.existsByNickIgnoreCase(candidate)) {
            candidate = base + suffix;
            suffix++;
        }
        return candidate;
    }
}
