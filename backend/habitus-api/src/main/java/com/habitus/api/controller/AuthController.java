package com.habitus.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.habitus.api.dto.request.ChangePasswordRequest;
import com.habitus.api.dto.request.LoginRequest;
import com.habitus.api.dto.request.RegisterRequest;
import com.habitus.api.dto.request.UpdateUserProfileRequest;
import com.habitus.api.dto.response.AuthResponse;
import com.habitus.api.dto.response.UserResponse;
import com.habitus.api.service.AuthService;
import com.habitus.api.service.CurrentUserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class AuthController {

    private final AuthService authService;
    private final CurrentUserService currentUserService;

    @PostMapping("/auth/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse registrar(@Valid @RequestBody RegisterRequest requisicao) {
        return authService.registrar(requisicao);
    }

    @PostMapping("/auth/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest requisicao) {
        return authService.login(requisicao);
    }

    @GetMapping("/users/me")
    public UserResponse usuarioAtual() {
        return authService.buscarUsuarioAtual(currentUserService.obterUsuarioAtual());
    }

    @PutMapping("/users/me")
    public UserResponse atualizarUsuarioAtual(@Valid @RequestBody UpdateUserProfileRequest requisicao) {
        return authService.atualizarUsuarioAtual(currentUserService.obterUsuarioAtual(), requisicao);
    }

    @PutMapping("/users/me/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void alterarSenha(@Valid @RequestBody ChangePasswordRequest requisicao) {
        authService.alterarSenha(currentUserService.obterUsuarioAtual(), requisicao);
    }
}
