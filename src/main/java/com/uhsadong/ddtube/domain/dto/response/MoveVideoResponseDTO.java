package com.uhsadong.ddtube.domain.dto.response;

import lombok.Builder;

@Builder
public record MoveVideoResponseDTO (
    boolean conflict,
    String videoCode,
    Long newPriority,
    Long oldPriority
) {

}
