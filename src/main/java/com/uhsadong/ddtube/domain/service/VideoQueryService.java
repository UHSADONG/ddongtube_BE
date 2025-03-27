package com.uhsadong.ddtube.domain.service;

import com.uhsadong.ddtube.domain.dto.response.UserDetailResponseDTO;
import com.uhsadong.ddtube.domain.dto.response.VideoDetailResponseDTO;
import com.uhsadong.ddtube.domain.entity.Video;
import com.uhsadong.ddtube.domain.repository.VideoRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VideoQueryService {

    private final VideoRepository videoRepository;

    public List<Video> getVideoListByPlaylistCode(String playlistCode) {
        return videoRepository.findAllByPlaylistCodeOrderByCreatedAt(playlistCode);
    }

    public List<VideoDetailResponseDTO> getVideoDetailListByPlaylistCode(String playlistCode) {
        List<Video> videoList = getVideoListByPlaylistCode(playlistCode);
        return videoList.stream()
            .map(video -> VideoDetailResponseDTO.builder()
                .user(UserDetailResponseDTO.builder()
                    .name(video.getUser().getName())
                    .code(video.getUser().getCode())
                    .isAdmin(video.getUser().isAdmin())
                    .build())
                .code(video.getCode())
                .title(video.getTitle())
                .authorName(video.getAuthorName())
                .url(video.getUrl())
                .height(video.getHeight())
                .width(video.getWidth())
                .thumbnailUrl(video.getThumbnailUrl())
                .thumbnailHeight(video.getThumbnailHeight())
                .thumbnailWidth(video.getThumbnailWidth())
                .createdAt(video.getCreatedAt())
                .build()
            )
            .toList();
    }
}