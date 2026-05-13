package com.habitus.api.dto.response;

public record AuthResponse(
    UserResponse user,
    String token,
    String tokenType
) {
}
