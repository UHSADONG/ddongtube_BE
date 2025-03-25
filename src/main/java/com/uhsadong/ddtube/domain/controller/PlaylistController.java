package com.uhsadong.ddtube.domain.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/playlist")
public class PlaylistController {

    @GetMapping("/{playlistId}")
    @Operation(summary = "재생목록 조회", description = "재생목록 조회 기능입니다.")
    public ResponseEntity<String> getPlaylist(
        @PathVariable Long playlistId
    ) {
        return ResponseEntity.ok("Hello, World!");
    }

    @PostMapping()
    @Operation(summary = "재생목록 생성", description = "재생목록 생성 기능입니다.")
    public ResponseEntity<String> createPlaylist() {
        return ResponseEntity.ok("Hello, World!");
    }

    @DeleteMapping("/{playlistId}")
    @Operation(summary = "재생목록 생성", description = "재생목록 생성 기능입니다.")
    public ResponseEntity<String> deletePlaylist(
        @PathVariable Long playlistId
    ) {
        return ResponseEntity.ok("Hello, World!");
    }
}
