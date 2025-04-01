package com.uhsadong.ddtube.domain.service;

import com.uhsadong.ddtube.domain.dto.response.UserDetailResponseDTO;
import com.uhsadong.ddtube.domain.dto.response.VideoDetailResponseDTO;
import com.uhsadong.ddtube.domain.entity.Video;
import com.uhsadong.ddtube.domain.repository.VideoRepository;
import com.uhsadong.ddtube.global.response.code.status.ErrorStatus;
import com.uhsadong.ddtube.global.response.exception.GeneralException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VideoQueryService {

    private final VideoRepository videoRepository;

    public List<Video> getVideoListByPlaylistCode(String playlistCode) {
        return videoRepository.findAllByPlaylistCodeOrderByPriority(playlistCode);
    }

    public List<VideoDetailResponseDTO> getVideoDetailListByPlaylistCode(String playlistCode) {
        List<Video> videoList = getVideoListByPlaylistCode(playlistCode);
        return videoList.stream().map(this::convertToVideoDetailResponseDTO).toList();
    }

    public Video getVideoByCodeOrThrow(String videoCode) {
        return videoRepository.findFirstByCode(videoCode)
            .orElseThrow(() -> new GeneralException(ErrorStatus._VIDEO_NOT_FOUND));
    }

    public VideoDetailResponseDTO convertToVideoDetailResponseDTO(Video video) {
        return VideoDetailResponseDTO.builder()
            .user(UserDetailResponseDTO.builder()
                .name(video.getUser().getName())
                .code(video.getUser().getCode())
                .isAdmin(video.getUser().isAdmin())
                .build())
            .code(video.getCode())
            .description(video.getDescription())
            .title(video.getTitle())
            .authorName(video.getAuthorName())
            .url(video.getUrl())
            .height(video.getHeight())
            .width(video.getWidth())
            .thumbnailUrl(video.getThumbnailUrl())
            .thumbnailHeight(video.getThumbnailHeight())
            .thumbnailWidth(video.getThumbnailWidth())
            .createdAt(video.getCreatedAt())
            .priority(video.getPriority())
            .build();
    }
}