package com.uhsadong.ddtube.global.sse.dto;

import lombok.Builder;

@Builder
public record ConnectionCreateSseResponseDTO(
    String userName,
    Long clientCount
) {

}
