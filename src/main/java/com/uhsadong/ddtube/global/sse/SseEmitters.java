package com.uhsadong.ddtube.global.sse;

import com.uhsadong.ddtube.domain.dto.response.VideoDetailResponseDTO;
import com.uhsadong.ddtube.domain.entity.Video;
import com.uhsadong.ddtube.domain.service.VideoQueryService;
import com.uhsadong.ddtube.global.response.code.status.ErrorStatus;
import com.uhsadong.ddtube.global.response.exception.GeneralException;
import com.uhsadong.ddtube.global.sse.dto.CreateVideoSseResponseDTO;
import com.uhsadong.ddtube.global.sse.dto.DeleteVideoSseResponsDTO;
import com.uhsadong.ddtube.global.sse.dto.UpdateVideoSseResponseDTO;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Component
@Slf4j
@RequiredArgsConstructor
public class SseEmitters {

    private final VideoQueryService videoQueryService;
    private final Map<String, List<SseEmitter>> emitterMap = new ConcurrentHashMap<>();

    SseEmitter add(String playlistCode, SseEmitter emitter) {
        if (!this.emitterMap.containsKey(playlistCode)) {
            this.emitterMap.put(playlistCode, new ArrayList<>());
        }
        this.emitterMap.get(playlistCode).add(emitter);
        log.info("ADD Emitter - Playlist: {}, Emitter: {}", playlistCode, emitter);
        log.info("emitter list: {}, count: {}", this.emitterMap.get(playlistCode),
            this.emitterMap.get(playlistCode).size());
        emitter.onCompletion(() -> {
            log.info("onCompletion callback");
            this.emitterMap.get(playlistCode).remove(emitter);
            log.info("emitter list: {}, count: {}", this.emitterMap.get(playlistCode),
                this.emitterMap.get(playlistCode).size());
        });
        emitter.onTimeout(() -> {
            log.info("onTimeout callback");
            this.emitterMap.get(playlistCode).remove(emitter);
            emitter.complete();
            log.info("emitter list: {}, count: {}", this.emitterMap.get(playlistCode),
                this.emitterMap.get(playlistCode).size());
        });

        emitter.onError((e) -> {
            log.error("Emitter error", e);
            emitter.completeWithError(e);
            this.emitterMap.get(playlistCode).remove(emitter);
            log.info("emitter list: {}, count: {}", this.emitterMap.get(playlistCode),
                this.emitterMap.get(playlistCode).size());
        });

        return emitter;
    }

    public void sendVideoEventToClients(String playlistCode, Video video, SseStatus status) {
        Object responseDTO = switch (status) {
            case ADD -> CreateVideoSseResponseDTO.builder()
                .video(videoQueryService.convertToVideoDetailResponseDTO(video))
                .videoCode(video.getCode())
                .priority(video.getPriority())
                .status(status)
                .build();
            case MOVE -> UpdateVideoSseResponseDTO.builder()
                .videoCode(video.getCode())
                .priority(video.getPriority())
                .status(status)
                .build();
            case DELETE -> DeleteVideoSseResponsDTO.builder()
                .videoCode(video.getCode())
                .priority(video.getPriority())
                .status(status)
                .build();
            default -> throw new GeneralException(ErrorStatus._SSE_STATUS_ERROR);
        };
        sendByPlaylistCode("video", playlistCode, responseDTO);
    }

    public void sendNowPlayingVideoEventToClients(String playlistCode, Video video) {
        VideoDetailResponseDTO responseDTO = videoQueryService.convertToVideoDetailResponseDTO(
            video);
        sendByPlaylistCode("playing", playlistCode, responseDTO);
    }

    private void sendByPlaylistCode(String event, String playlistCode, Object responseDTO) {
        List<SseEmitter> emitters = emitterMap.get(playlistCode);
        if (emitters != null) {
            for (SseEmitter emitter : new ArrayList<>(emitters)) {
                try {
                    emitter.send(SseEmitter.event()
                        .name(event)
                        .data(responseDTO));
                } catch (Exception e) {
                    log.info("SSE send error", e);
                    emitter.completeWithError(e); // 연결 종료
                    emitters.remove(emitter);
                }
            }
        }
    }
}
