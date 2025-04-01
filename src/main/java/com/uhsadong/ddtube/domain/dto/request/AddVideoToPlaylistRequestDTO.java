package com.uhsadong.ddtube.domain.dto.request;

import org.hibernate.validator.constraints.Length;

public record AddVideoToPlaylistRequestDTO(
    @Length(min = 2, max = 300, message = "주소는 최대 300자까지 입력이 가능합니다.")
    String videoUrl,
    @Length(max = 100, message = "설명은 100자 이하여야 합니다.")
    String videoDescription
) {

}
