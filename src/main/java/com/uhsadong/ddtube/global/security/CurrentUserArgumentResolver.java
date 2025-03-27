package com.uhsadong.ddtube.global.security;

import com.uhsadong.ddtube.domain.entity.User;
import com.uhsadong.ddtube.domain.repository.UserRepository;
import com.uhsadong.ddtube.global.response.code.status.ErrorStatus;
import com.uhsadong.ddtube.global.response.exception.GeneralException;
import com.uhsadong.ddtube.global.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@RequiredArgsConstructor
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

    private final JwtUtil jwtUtil;

    private final UserRepository userRepository;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterAnnotation(CurrentUser.class) != null
            && parameter.getParameterType().equals(User.class);
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

    public User loadUserFromToken(String token) {
        try {
            String userCode = jwtUtil.getUserCodeInJwt(token);

            return userRepository.findByCode(userCode)
                .orElseThrow(() -> new GeneralException(ErrorStatus._USER_NOT_FOUND));
        } catch (Exception ex) {
            throw new GeneralException(ErrorStatus._INVALID_JWT);
        }
    }
}