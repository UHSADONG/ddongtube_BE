package com.uhsadong.ddtube.global.sse.dto;

import lombok.Builder;

@Builder
public record ConnectionCountSseResponseDTO(
        Long clientCount
) {

}
