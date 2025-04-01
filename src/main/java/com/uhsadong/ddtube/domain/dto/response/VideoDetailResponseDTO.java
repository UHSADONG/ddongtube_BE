package com.uhsadong.ddtube.domain.dto.response;

import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record VideoDetailResponseDTO(
    UserDetailResponseDTO user,
    String code,
    String description,
    String title,
    String authorName,
    String url,
    Integer height,
    Integer width,
    String thumbnailUrl,
    Integer thumbnailHeight,
    Integer thumbnailWidth,
    LocalDateTime createdAt
    ) {

}
