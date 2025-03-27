package com.uhsadong.ddtube.domain.dto.response;

import java.util.List;
import lombok.Builder;

@Builder
public record PlaylistDetailResponseDTO(
    String title,
    List<VideoDetailResponseDTO> videoList
) {

}
