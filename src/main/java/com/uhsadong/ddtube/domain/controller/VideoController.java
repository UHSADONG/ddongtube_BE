package com.uhsadong.ddtube.domain.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/video")
public class VideoController {

    @PostMapping("/{playlistId}")
    @Operation(summary = "재생목록에 영상 추가", description = "재생목록에 영상을 추가하는 기능입니다.")
    public ResponseEntity<String> addVideoToPlaylist(
        @PathVariable Long playlistId
    ) {
        return ResponseEntity.ok(Long.toString(playlistId));
    }

    @DeleteMapping("/{playlistId}/{videoId}")
    @Operation(summary = "재생목록에서 영상 제거", description = "재생목록에 영상을 제거하는 기능입니다.")
    public ResponseEntity<String> deleteVideoInPlaylist(
        @PathVariable Long playlistId,
        @PathVariable Long videoId
    ) {
        return ResponseEntity.ok(Long.toString(playlistId));
    }

}