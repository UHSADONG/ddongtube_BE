package com.uhsadong.ddtube.global.security;

import com.uhsadong.ddtube.domain.dto.UserSimpleDTO;
import com.uhsadong.ddtube.global.logger.enums.MdcKey;
import com.uhsadong.ddtube.global.response.code.status.ErrorStatus;
import com.uhsadong.ddtube.global.response.exception.GeneralException;
import com.uhsadong.ddtube.global.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jboss.logging.MDC;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Slf4j
@RequiredArgsConstructor
public class CurrentUserCodeArgumentResolver implements HandlerMethodArgumentResolver {

    private final JwtUtil jwtUtil;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterAnnotation(CurrentUserCode.class) != null
            && parameter.getParameterType().equals(UserSimpleDTO.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
        NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {

        String authorizationHeader = webRequest.getHeader("authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7); // "Bearer " 이후 문자열
            return loadUserFromToken(token);
        }
        throw new GeneralException(ErrorStatus._EMPTY_JWT);
    }

    public UserSimpleDTO loadUserFromToken(String token) {
        try {

            UserSimpleDTO userDTO = jwtUtil.getUserSimpleDataInJwt(token);
            MDC.put(MdcKey.USER_ID.name(), userDTO.userCode());
            return userDTO;

        } catch (Exception ex) {
            throw new GeneralException(ErrorStatus._INVALID_JWT);
        }
    }
}