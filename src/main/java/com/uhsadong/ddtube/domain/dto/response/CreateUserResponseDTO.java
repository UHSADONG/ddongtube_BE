package com.uhsadong.ddtube.domain.dto.response;

import lombok.Builder;

@Builder
public record CreateUserResponseDTO(
    String accessToken,
    boolean isAdmin
) {
}
