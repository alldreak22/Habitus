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

    public User getCurrentUser() {
        HttpServletRequest request = currentRequest();
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new UnauthorizedException("Missing bearer token");
        }

        Long userId = tokenService.readUserId(authorization.substring("Bearer ".length()));
        return userRepository.findById(userId)
            .orElseThrow(() -> new UnauthorizedException("User not found for token"));
    }

    private HttpServletRequest currentRequest() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            return attributes.getRequest();
        }
        throw new UnauthorizedException("No active request");
    }
}
