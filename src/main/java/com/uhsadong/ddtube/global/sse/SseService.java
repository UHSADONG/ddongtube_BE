package com.uhsadong.ddtube.global.sse;

import com.uhsadong.ddtube.domain.dto.response.VideoDetailResponseDTO;
import com.uhsadong.ddtube.domain.entity.Video;
import com.uhsadong.ddtube.domain.service.VideoQueryService;
import com.uhsadong.ddtube.global.response.code.status.ErrorStatus;
import com.uhsadong.ddtube.global.response.exception.GeneralException;
import com.uhsadong.ddtube.global.sse.dto.ConnectionCountSseResponseDTO;
import com.uhsadong.ddtube.global.sse.dto.CreateVideoSseResponseDTO;
import com.uhsadong.ddtube.global.sse.dto.DeleteVideoSseResponsDTO;
import com.uhsadong.ddtube.global.sse.dto.UpdateVideoSseResponseDTO;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@Service
@RequiredArgsConstructor
public class SseService {

    private final VideoQueryService videoQueryService;
    private final Map<String, List<SseEmitter>> emitterMap = new ConcurrentHashMap<>();

    SseEmitter add(String playlistCode, String userCode, SseEmitter emitter) {
        if (!this.emitterMap.containsKey(playlistCode)) {
            this.emitterMap.put(playlistCode, new ArrayList<>());
        }
        this.emitterMap.get(playlistCode).add(emitter);
        log.info("[    CONN] Playlist {} | User {} | Emitter {} | TotalConnect {}"
            , playlistCode, userCode, emitter, this.emitterMap.get(playlistCode).size());
        emitter.onCompletion(() -> {
            this.emitterMap.get(playlistCode).remove(emitter);
            log.info("[ DISCONN] COMPLETION | Playlist {} | User {} | Emitter {} | TotalConnect {}"
                , playlistCode, userCode, emitter, this.emitterMap.get(playlistCode).size());
            }
        );
        emitter.onTimeout(() -> {
            emitter.complete();
            this.emitterMap.get(playlistCode).remove(emitter);
            log.info("[ DISCONN] TIMEOUT | Playlist {} | User {} | Emitter {} | TotalConnect {}"
                , playlistCode, userCode, emitter, this.emitterMap.get(playlistCode).size());
        });

        emitter.onError((e) -> {
            emitter.completeWithError(e);
            this.emitterMap.get(playlistCode).remove(emitter);
            log.info("[ DISCONN] ERROR | Playlist {} | User {} | Emitter {} | TotalConnect {} | Error {}"
                , playlistCode, userCode, emitter, this.emitterMap.get(playlistCode).size()
                , e.getMessage());
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

    public void sendPingWithConnectionCount() {
        for (Map.Entry<String, List<SseEmitter>> entry : emitterMap.entrySet()) {
            List<SseEmitter> emitters = entry.getValue();
            ConnectionCountSseResponseDTO responseDTO = ConnectionCountSseResponseDTO.builder()
                .clientCount((long) emitters.size())
                .build();
            for (SseEmitter emitter : new ArrayList<>(emitters)) {
                try {
                    emitter.send(SseEmitter.event()
                        .name("ping")
                        .data(responseDTO));
                } catch (Exception e) {
                    emitter.completeWithError(e); // 연결 종료
                    emitters.remove(emitter);
                }
            }
        }
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
                    emitter.completeWithError(e); // 연결 종료
                    emitters.remove(emitter);
                }
            }
        }
    }
}
