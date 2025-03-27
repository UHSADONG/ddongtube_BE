package com.uhsadong.ddtube.domain.controller;

import com.uhsadong.ddtube.domain.dto.request.AddVideoToPlaylistRequestDTO;
import com.uhsadong.ddtube.domain.service.VideoCommandService;
import com.uhsadong.ddtube.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/video")
public class VideoController {

    private final VideoCommandService videoCommandService;

    @PostMapping("/{playlistCode}")
    @Operation(summary = "재생목록에 영상 추가", description = "재생목록에 영상을 추가하는 기능입니다.")
    public ResponseEntity<ApiResponse<String>> addVideoToPlaylist(
        @PathVariable String playlistCode,
        @RequestBody @Valid AddVideoToPlaylistRequestDTO addVideoToPlaylistRequestDTO
    ) {
        videoCommandService.addVideoToPlaylist(playlistCode, addVideoToPlaylistRequestDTO);
        return ResponseEntity.ok(ApiResponse.onSuccess("추가 완료"));
    }

    @DeleteMapping("/{playlistCode}/{videoId}")
    @Operation(summary = "재생목록에서 영상 제거", description = "재생목록에 영상을 제거하는 기능입니다.")
    public ResponseEntity<ApiResponse<String>> deleteVideoInPlaylist(
        @PathVariable Long playlistCode,
        @PathVariable Long videoId
    ) {
        return ResponseEntity.ok(ApiResponse.onSuccess(Long.toString(playlistCode)));
    }

}