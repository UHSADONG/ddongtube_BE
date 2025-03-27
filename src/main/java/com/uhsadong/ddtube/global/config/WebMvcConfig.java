package com.uhsadong.ddtube.global.config;

import com.uhsadong.ddtube.domain.repository.UserRepository;
import com.uhsadong.ddtube.global.security.CurrentUserArgumentResolver;
import com.uhsadong.ddtube.global.util.JwtUtil;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new CurrentUserArgumentResolver(jwtUtil, userRepository));
    }
}