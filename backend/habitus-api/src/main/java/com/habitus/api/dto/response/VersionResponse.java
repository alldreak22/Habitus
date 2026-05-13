package com.habitus.api.dto.response;

public record VersionResponse(
    String name,
    String version,
    String displayName
) {
}
