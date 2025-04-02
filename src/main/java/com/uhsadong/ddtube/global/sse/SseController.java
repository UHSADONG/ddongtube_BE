package com.uhsadong.ddtube.global.sse;

import com.uhsadong.ddtube.domain.entity.User;
import com.uhsadong.ddtube.global.response.code.status.ErrorStatus;
import com.uhsadong.ddtube.global.response.exception.GeneralException;
import com.uhsadong.ddtube.global.security.CurrentUser;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/sse")
@Slf4j
public class SseController {

    private final SseEmitters sseEmitters;
    @Value("${ddtube.sse.time_out}")
    private long TIME_OUT;

    public SseController(SseEmitters sseEmitters) {
        this.sseEmitters = sseEmitters;
    }

    @GetMapping(value = "/{playlistCode}/connect", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter connect(
        @CurrentUser User user,
        @PathVariable String playlistCode
    ) {
        SseEmitter emitter = new SseEmitter(TIME_OUT); // timeout 30분
        sseEmitters.add(playlistCode, emitter);
        try {
            emitter.send(SseEmitter.event()
                .name("connect")
                .data("connected!"));
        } catch (IOException e) {
            throw new GeneralException(ErrorStatus._SSE_CONNECTION_ERROR);
        }
        return emitter;
    }
}