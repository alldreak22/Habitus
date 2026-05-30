package com.habitus.api.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.habitus.api.dto.request.LoginRequest;
import com.habitus.api.dto.request.RegisterRequest;
import com.habitus.api.dto.request.UpdateUserProfileRequest;
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
            throw new ApiException(HttpStatus.CONFLICT, "E-mail ja cadastrado");
        }
        String nick = requisicao.nick().trim();
        if (userRepository.existsByNickIgnoreCase(nick)) {
            throw new ApiException(HttpStatus.CONFLICT, "Usuario ja cadastrado");
        }

        User user = new User();
        user.setName(requisicao.name().trim());
        user.setEmail(email);
        user.setNick(nick);
        user.setPassword(passwordEncoder.encode(requisicao.password()));
        User savedUser = userRepository.save(user);

        return respostaAutenticacao(savedUser);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest requisicao) {
        String login = requisicao.login().trim();
        String normalizedLogin = login.toLowerCase();
        User user = userRepository.findByEmailIgnoreCaseOrNickIgnoreCase(normalizedLogin, login)
            .orElseThrow(() -> new UnauthorizedException("Login ou senha invalidos"));

        if (!passwordEncoder.matches(requisicao.password(), user.getPassword())) {
            throw new UnauthorizedException("Login ou senha invalidos");
        }

        return respostaAutenticacao(user);
    }

    @Transactional(readOnly = true)
    public UserResponse buscarUsuarioAtual(User user) {
        return mapper.toUserResponse(user);
    }

    @Transactional
    public UserResponse atualizarUsuarioAtual(User user, UpdateUserProfileRequest requisicao) {
        String normalizedEmail = normalizarEmail(requisicao.email());
        userRepository.findByEmailIgnoreCase(normalizedEmail)
            .filter(existingUser -> !existingUser.getId().equals(user.getId()))
            .ifPresent((existingUser) -> {
                throw new ApiException(HttpStatus.CONFLICT, "E-mail ja cadastrado");
            });

        user.setName(requisicao.name().trim());
        user.setEmail(normalizedEmail);
        if (requisicao.nick() != null && !requisicao.nick().isBlank()) {
            user.setNick(requisicao.nick().trim());
        }
        user.setPicture(requisicao.picture());

        User savedUser = userRepository.save(user);
        return mapper.toUserResponse(savedUser);
    }

    private AuthResponse respostaAutenticacao(User user) {
        return new AuthResponse(mapper.toUserResponse(user), tokenService.criarToken(user.getId()), "Bearer");
    }

    private String normalizarEmail(String email) {
        return email.trim().toLowerCase();
    }
}
