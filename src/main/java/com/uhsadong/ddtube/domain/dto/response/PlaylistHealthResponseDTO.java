package com.uhsadong.ddtube.domain.dto.response;

import com.uhsadong.ddtube.domain.enums.PlaylistHealth;
import lombok.Builder;

@Builder
public record PlaylistHealthResponseDTO(
    PlaylistHealth health,
    String playlistCode
) {

}
