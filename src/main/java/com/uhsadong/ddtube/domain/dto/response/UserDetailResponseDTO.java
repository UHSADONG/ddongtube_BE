package com.uhsadong.ddtube.domain.dto.response;

import lombok.Builder;

@Builder
public record UserDetailResponseDTO(
    String code,
    String name,
    boolean isAdmin
) {

}
