package com.uhsadong.ddtube.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.uhsadong.ddtube.domain.dto.response.VideoDetailResponseDTO;
import com.uhsadong.ddtube.domain.entity.Playlist;
import com.uhsadong.ddtube.domain.entity.User;
import com.uhsadong.ddtube.domain.entity.Video;
import com.uhsadong.ddtube.domain.service.VideoQueryService;
import com.uhsadong.ddtube.global.sse.SseService;
import com.uhsadong.ddtube.global.sse.SseStatus;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@ExtendWith(MockitoExtension.class)
class SseServiceTest {

    @InjectMocks
    private SseService sseService;

    @Mock
    private VideoQueryService videoQueryService;

    @Test
    @DisplayName("비디오 이벤트 발송 - 추가 이벤트를 모든 클라이언트에게 발송한다")
    void sendVideoEventToClients_addEvent_success() throws IOException {
        // given
        String playlistCode = "playlistCode";
        SseStatus status = SseStatus.ADD;

        Playlist playlist = Playlist.toEntity(
            playlistCode,
            "Test Playlist",
            "Test Description",
            "https://example.com/thumbnail.jpg",
            LocalDateTime.now()
        );

        User user = User.toEntity(playlist, "userCode", "testUser", "password", false);

        Video video = new Video();
        ReflectionTestUtils.setField(video, "code", "videoCode");
        ReflectionTestUtils.setField(video, "priority", 1000L);
        ReflectionTestUtils.setField(video, "user", user);

        VideoDetailResponseDTO videoDetailResponseDTO = VideoDetailResponseDTO.builder()
            .code("videoCode")
            .title("Test Video")
            .priority(1000L)
            .build();

        SseEmitter emitter1 = mock(SseEmitter.class);
        SseEmitter emitter2 = mock(SseEmitter.class);
        List<SseEmitter> emitters = new ArrayList<>();
        emitters.add(emitter1);
        emitters.add(emitter2);

        Map<String, List<SseEmitter>> emitterMap = new ConcurrentHashMap<>();
        emitterMap.put(playlistCode, emitters);

        ReflectionTestUtils.setField(sseService, "emitterMap", emitterMap);

        when(videoQueryService.convertToVideoDetailResponseDTO(video)).thenReturn(
            videoDetailResponseDTO);

        // when
        sseService.sendVideoEventToClients(playlistCode, video, status);

        // then
        verify(videoQueryService).convertToVideoDetailResponseDTO(video);
        verify(emitter1, times(1)).send(any(SseEmitter.SseEventBuilder.class));
        verify(emitter2, times(1)).send(any(SseEmitter.SseEventBuilder.class));
    }

    @Test
    @DisplayName("비디오 이벤트 발송 - 삭제 이벤트를 모든 클라이언트에게 발송한다")
    void sendVideoEventToClients_deleteEvent_success() throws IOException {
        // given
        String playlistCode = "playlistCode";
        SseStatus status = SseStatus.DELETE;

        Playlist playlist = Playlist.toEntity(
            playlistCode,
            "Test Playlist",
            "Test Description",
            "https://example.com/thumbnail.jpg",
            LocalDateTime.now()
        );

        User user = User.toEntity(playlist, "userCode", "testUser", "password", false);

        Video video = new Video();
        ReflectionTestUtils.setField(video, "code", "videoCode");
        ReflectionTestUtils.setField(video, "priority", 1000L);
        ReflectionTestUtils.setField(video, "user", user);

        SseEmitter emitter1 = mock(SseEmitter.class);
        SseEmitter emitter2 = mock(SseEmitter.class);
        List<SseEmitter> emitters = new ArrayList<>();
        emitters.add(emitter1);
        emitters.add(emitter2);

        Map<String, List<SseEmitter>> emitterMap = new ConcurrentHashMap<>();
        emitterMap.put(playlistCode, emitters);

        ReflectionTestUtils.setField(sseService, "emitterMap", emitterMap);

        // when
        sseService.sendVideoEventToClients(playlistCode, video, status);

        // then
        verify(emitter1, times(1)).send(any(SseEmitter.SseEventBuilder.class));
        verify(emitter2, times(1)).send(any(SseEmitter.SseEventBuilder.class));
    }

    @Test
    @DisplayName("비디오 이벤트 발송 - 이동 이벤트를 모든 클라이언트에게 발송한다")
    void sendVideoEventToClients_moveEvent_success() throws IOException {
        // given
        String playlistCode = "playlistCode";
        SseStatus status = SseStatus.MOVE;

        Playlist playlist = Playlist.toEntity(
            playlistCode,
            "Test Playlist",
            "Test Description",
            "https://example.com/thumbnail.jpg",
            LocalDateTime.now()
        );

        User user = User.toEntity(playlist, "userCode", "testUser", "password", false);

        Video video = new Video();
        ReflectionTestUtils.setField(video, "code", "videoCode");
        ReflectionTestUtils.setField(video, "priority", 1000L);
        ReflectionTestUtils.setField(video, "user", user);

        SseEmitter emitter1 = mock(SseEmitter.class);
        SseEmitter emitter2 = mock(SseEmitter.class);
        List<SseEmitter> emitters = new ArrayList<>();
        emitters.add(emitter1);
        emitters.add(emitter2);

        Map<String, List<SseEmitter>> emitterMap = new ConcurrentHashMap<>();
        emitterMap.put(playlistCode, emitters);

        ReflectionTestUtils.setField(sseService, "emitterMap", emitterMap);

        // when
        sseService.sendVideoEventToClients(playlistCode, video, status);

        // then
        verify(emitter1, times(1)).send(any(SseEmitter.SseEventBuilder.class));
        verify(emitter2, times(1)).send(any(SseEmitter.SseEventBuilder.class));
    }

    @Test
    @DisplayName("현재 재생 비디오 이벤트 발송 - 모든 클라이언트에게 발송한다")
    void sendNowPlayingVideoEventToClients_success() throws IOException {
        // given
        String playlistCode = "playlistCode";
        String videoCode = "videoCode";
        String userName = "testUser";
        Boolean autoPlay = true;

        Video video = new Video();
        ReflectionTestUtils.setField(video, "code", videoCode);

        SseEmitter emitter1 = mock(SseEmitter.class);
        SseEmitter emitter2 = mock(SseEmitter.class);
        List<SseEmitter> emitters = new ArrayList<>();
        emitters.add(emitter1);
        emitters.add(emitter2);

        Map<String, List<SseEmitter>> emitterMap = new ConcurrentHashMap<>();
        emitterMap.put(playlistCode, emitters);

        ReflectionTestUtils.setField(sseService, "emitterMap", emitterMap);

        // when
        sseService.sendNowPlayingVideoEventToClients(playlistCode, video, userName, autoPlay);

        // then
        verify(emitter1, times(1)).send(any(SseEmitter.SseEventBuilder.class));
        verify(emitter2, times(1)).send(any(SseEmitter.SseEventBuilder.class));
    }

    @Test
    @DisplayName("연결 수 핑 이벤트 발송 - 모든 클라이언트에게 발송한다")
    void sendPingWithConnectionCount_success() throws IOException {
        // given
        String playlistCode1 = "playlistCode1";
        String playlistCode2 = "playlistCode2";

        SseEmitter emitter1 = mock(SseEmitter.class);
        SseEmitter emitter2 = mock(SseEmitter.class);
        List<SseEmitter> emitters1 = new ArrayList<>();
        emitters1.add(emitter1);

        SseEmitter emitter3 = mock(SseEmitter.class);
        List<SseEmitter> emitters2 = new ArrayList<>();
        emitters2.add(emitter2);
        emitters2.add(emitter3);

        Map<String, List<SseEmitter>> emitterMap = new ConcurrentHashMap<>();
        emitterMap.put(playlistCode1, emitters1);
        emitterMap.put(playlistCode2, emitters2);

        ReflectionTestUtils.setField(sseService, "emitterMap", emitterMap);

        // when
        sseService.sendPingWithConnectionCount();

        // then
        verify(emitter1, times(1)).send(any(SseEmitter.SseEventBuilder.class));
        verify(emitter2, times(1)).send(any(SseEmitter.SseEventBuilder.class));
        verify(emitter3, times(1)).send(any(SseEmitter.SseEventBuilder.class));
    }

    @Test
    @DisplayName("연결 수 핑 이벤트 발송 - 에러가 발생한 이미터는 제거된다")
    void sendPingWithConnectionCount_removeErrorEmitter() throws IOException {
        // given
        String playlistCode = "playlistCode";

        SseEmitter emitter1 = mock(SseEmitter.class);
        SseEmitter emitter2 = mock(SseEmitter.class);
        List<SseEmitter> emitters = new ArrayList<>();
        emitters.add(emitter1);
        emitters.add(emitter2);

        Map<String, List<SseEmitter>> emitterMap = new ConcurrentHashMap<>();
        emitterMap.put(playlistCode, emitters);

        ReflectionTestUtils.setField(sseService, "emitterMap", emitterMap);

        Mockito.doThrow(new IOException("Test exception")).when(emitter1)
            .send(any(SseEmitter.SseEventBuilder.class));

        // when
        sseService.sendPingWithConnectionCount();

        // then
        verify(emitter1, times(1)).send(any(SseEmitter.SseEventBuilder.class));
        verify(emitter1, times(1)).completeWithError(any(IOException.class));
        verify(emitter2, times(1)).send(any(SseEmitter.SseEventBuilder.class));
    }
}
