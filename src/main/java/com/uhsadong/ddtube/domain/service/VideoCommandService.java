package com.uhsadong.ddtube.domain.service;

import com.uhsadong.ddtube.domain.dto.YoutubeOEmbedDTO;
import com.uhsadong.ddtube.domain.dto.request.AddVideoToPlaylistRequestDTO;
import com.uhsadong.ddtube.domain.entity.Playlist;
import com.uhsadong.ddtube.domain.entity.User;
import com.uhsadong.ddtube.domain.entity.Video;
import com.uhsadong.ddtube.domain.repository.VideoRepository;
import com.uhsadong.ddtube.global.util.YoutubeOEmbed;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class VideoCommandService {

    private final VideoRepository videoRepository;
    private final PlaylistQueryService playlistQueryService;

    @Transactional
    public void addVideoToPlaylist(
        User user, String playlistCode, AddVideoToPlaylistRequestDTO requestDTO) {
        log.info(user.getCode());

        Playlist playlist = playlistQueryService.getPlaylistByCodeOrThrow(playlistCode);

        YoutubeOEmbedDTO youtubeOEmbedDTO = YoutubeOEmbed.getVideoInfo(requestDTO.videoUrl());
        videoRepository.save(
            Video.toEntity(playlist, user, requestDTO.videoUrl(), youtubeOEmbedDTO)
        );
    }

}
