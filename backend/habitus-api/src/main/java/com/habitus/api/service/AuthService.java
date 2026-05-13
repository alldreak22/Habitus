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
    public AuthResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ApiException(HttpStatus.CONFLICT, "Email is already registered");
        }

        User user = new User();
        user.setName(request.name().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.password()));
        User savedUser = userRepository.save(user);

        return authResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmailIgnoreCase(normalizeEmail(request.email()))
            .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        return authResponse(user);
    }

    @Transactional(readOnly = true)
    public UserResponse me(User user) {
        return mapper.toUserResponse(user);
    }

    private AuthResponse authResponse(User user) {
        return new AuthResponse(mapper.toUserResponse(user), tokenService.createToken(user.getId()), "Bearer");
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }
}
