package com.uhsadong.ddtube.domain.dto.response;

import lombok.Builder;

@Builder
public record PlaylistPublicMetaResponseDTO(
    String title,
    String thumbnailUrl,
    String description
) {

}
