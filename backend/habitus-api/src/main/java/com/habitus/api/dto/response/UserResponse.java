package com.habitus.api.dto.response;

public record UserResponse(
    Long id,
    String name,
    String email,
    String nick,
    String picture
) {
}
