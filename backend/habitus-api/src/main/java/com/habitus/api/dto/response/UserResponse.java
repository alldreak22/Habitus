package com.habitus.api.dto.response;

import java.time.LocalDateTime;

public record UserResponse(
    Long id,
    String name,
    String email,
    String nick,
    String picture,
    LocalDateTime createdAt
) {
}
