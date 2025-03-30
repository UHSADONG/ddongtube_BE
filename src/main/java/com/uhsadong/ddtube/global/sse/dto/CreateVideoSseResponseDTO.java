package com.uhsadong.ddtube.global.sse.dto;

import com.uhsadong.ddtube.domain.dto.response.VideoDetailResponseDTO;
import com.uhsadong.ddtube.global.sse.SseStatus;
import lombok.Builder;

@Builder
public record CreateVideoSseResponseDTO(
    VideoDetailResponseDTO video,
    String videoCode,
    Long priority,
    SseStatus status
) {

}
