package com.uhsadong.ddtube.domain.dto.response;

import java.util.List;
import lombok.Builder;

@Builder
public record PlaylistMetaResponseDTO(
    String title,
    String thumbnailUrl,
    String description,
    String owner,
    List<String> userList
) {

}
