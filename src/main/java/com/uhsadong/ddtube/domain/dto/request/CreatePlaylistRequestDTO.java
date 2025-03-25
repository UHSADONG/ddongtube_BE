package com.uhsadong.ddtube.domain.dto.request;

import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.Length;

public record CreatePlaylistRequestDTO(
    @Length(min = 2, max = 20, message = "이름은 2자 이상 20자 이하여야 합니다.")
    String userName, // 만든사람 닉네임
    @Length(min = 2, max = 100, message = "비밀번호는 2자 이상 100자 이하여야 합니다.")
    String userPassword, // 만든사람 비밀번호
    @Pattern(regexp = "\\d{4}", message = "PIN은 4자리 숫자여야 합니다.")
    String playlistPin, // 4자리 숫자
    @Length(min = 1, max = 100, message = "재생목록 제목은 1자 이상 100자 이하여야 합니다.")
    String playlistTitle // 재생목록 제목
) {

}
