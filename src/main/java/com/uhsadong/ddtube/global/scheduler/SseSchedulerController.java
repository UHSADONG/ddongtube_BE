package com.uhsadong.ddtube.global.scheduler;

import com.uhsadong.ddtube.global.sse.SseEmitters;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SseSchedulerController {


    private final SseEmitters sseEmitters;


    @Scheduled(fixedDelay = 60 * 1000) // 함수 종료 후 1분
    public void sendSsePingScheduler() {
        sseEmitters.sendPingWithConnectionCount();
    }

}
