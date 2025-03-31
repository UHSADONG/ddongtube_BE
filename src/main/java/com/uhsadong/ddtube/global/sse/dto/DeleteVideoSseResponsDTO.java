package com.uhsadong.ddtube.global.sse.dto;

import com.uhsadong.ddtube.global.sse.SseStatus;
import lombok.Builder;

@Builder
public record DeleteVideoSseResponsDTO(
    String videoCode,
    Long priority,
    SseStatus status
) {

}
