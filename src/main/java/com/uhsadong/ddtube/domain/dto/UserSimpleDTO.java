package com.uhsadong.ddtube.domain.dto;

import lombok.Builder;

@Builder
public record UserSimpleDTO(
    String userCode,
    String playlistCode,
    String userName
) {

}
