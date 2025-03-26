package com.uhsadong.ddtube.domain.controller;

import com.uhsadong.ddtube.domain.dto.request.CreatePlaylistRequestDTO;
import com.uhsadong.ddtube.domain.service.PlaylistCommandService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/playlist")
public class PlaylistController {

    private final PlaylistCommandService playlistCommandService;

    @GetMapping("/{playlistId}")
    @Operation(summary = "재생목록 조회", description = "재생목록 조회 기능입니다.")
    public ResponseEntity<String> getPlaylist(
        @PathVariable Long playlistId,
        @RequestParam(required = false) String pin
    ) {
        return ResponseEntity.ok("Hello, World!");
    }

    @PostMapping()
    @Operation(summary = "[0326] 재생목록 생성", description = "재생목록을 생성합니다. 생성하는 유저는 필수적으로 name과 password를 입력해야합니다.")
    public ResponseEntity<String> createPlaylist(
        @RequestBody @Valid CreatePlaylistRequestDTO createPlaylistRequestDTO
    ) {
        return ResponseEntity.ok(
            playlistCommandService.createPlaylist(createPlaylistRequestDTO)
        );
    }

    @DeleteMapping("/{playlistId}")
    @Operation(summary = "재생목록 삭제", description = "재생목록 삭제 기능입니다.")
    public ResponseEntity<String> deletePlaylist(
        @PathVariable Long playlistId
    ) {
        return ResponseEntity.ok("Hello, World!");
    }
}
