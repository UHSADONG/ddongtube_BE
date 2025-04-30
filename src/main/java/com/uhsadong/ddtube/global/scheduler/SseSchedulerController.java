package com.uhsadong.ddtube.global.scheduler;

import com.uhsadong.ddtube.domain.service.PlaylistCommandService;
import com.uhsadong.ddtube.global.sse.SseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SseSchedulerController {


    private final SseService sseService;
    private final PlaylistCommandService playlistCommandService;


    @Scheduled(fixedDelay = 60 * 1000) // 함수 종료 후 1분
    public void sendSsePingScheduler() {
        sseService.sendPingWithConnectionCount();
    }

}
