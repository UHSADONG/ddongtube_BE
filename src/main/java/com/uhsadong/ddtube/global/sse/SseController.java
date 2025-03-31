package com.uhsadong.ddtube.global.sse;

import com.uhsadong.ddtube.global.response.ApiResponse;
import com.uhsadong.ddtube.global.response.code.status.ErrorStatus;
import com.uhsadong.ddtube.global.response.exception.GeneralException;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/sse")
@Slf4j
public class SseController {

    private final SseEmitters sseEmitters;

    public SseController(SseEmitters sseEmitters) {
        this.sseEmitters = sseEmitters;
    }

    @GetMapping(value = "/{playlistCode}/connect", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter connect(
        @PathVariable String playlistCode
    ) {
        SseEmitter emitter = new SseEmitter();
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