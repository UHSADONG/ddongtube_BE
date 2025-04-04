package com.uhsadong.ddtube.global.sse.dto;

import lombok.Builder;

@Builder
public record UpdatePlayingVideoSseResponseDTO(
    String videoCode,
    String userName
) {

}
