package com.uhsadong.ddtube.global.sse.dto;

import com.uhsadong.ddtube.global.sse.SseStatus;
import lombok.Builder;

@Builder
public record UpdateVideoSseResponseDTO (
    String videoCode,
    Long priority,
    SseStatus status
){

}