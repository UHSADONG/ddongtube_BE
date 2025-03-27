package com.uhsadong.ddtube.domain.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import org.hibernate.validator.constraints.Length;

@Builder
public record CreateUserRequestDTO(
    @NotNull
    @Length(min = 2, max = 20, message = "이름은 2자 이상 20자 이하여야 합니다.")
    String name,
    @NotNull
    @Length(max = 100, message = "비밀번호는 100자 이하여야 합니다.")
    String password
) {

}
