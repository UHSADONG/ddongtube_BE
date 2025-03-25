package com.uhsadong.ddtube.domain.service;

import com.uhsadong.ddtube.domain.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VideoCommandService {
    private final VideoRepository videoRepository;

}
