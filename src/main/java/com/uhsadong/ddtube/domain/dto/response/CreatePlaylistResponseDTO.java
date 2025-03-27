package com.uhsadong.ddtube.domain.dto.response;

import lombok.Builder;

@Builder
public record CreatePlaylistResponseDTO(
    String playlistCode,
    String accessToken
) {

}
