package com.uhsadong.ddtube.domain.controller;

import com.uhsadong.ddtube.domain.dto.request.AddVideoToPlaylistRequestDTO;
import com.uhsadong.ddtube.domain.dto.response.MoveVideoResponseDTO;
import com.uhsadong.ddtube.domain.entity.User;
import com.uhsadong.ddtube.domain.service.VideoCommandService;
import com.uhsadong.ddtube.global.response.ApiResponse;
import com.uhsadong.ddtube.global.security.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/video")
public class VideoController {

    private final VideoCommandService videoCommandService;

    @PostMapping("/{playlistCode}")
    @Operation(summary = "재생목록에 영상 추가", description = "재생목록에 영상을 추가하는 기능입니다.")
    public ResponseEntity<ApiResponse<String>> addVideoToPlaylist(
        @CurrentUser User user,
        @PathVariable String playlistCode,
        @RequestBody @Valid AddVideoToPlaylistRequestDTO addVideoToPlaylistRequestDTO
    ) {
        videoCommandService.addVideoToPlaylist(user, playlistCode, addVideoToPlaylistRequestDTO);
        return ResponseEntity.ok(ApiResponse.onSuccess("추가 완료"));
    }

    @DeleteMapping("/{playlistCode}/{videoCode}")
    @Operation(summary = "재생목록에서 영상 제거", description = "재생목록에 영상을 제거하는 기능입니다. \n관리자는 모든 영상을 지울 수 있으며, 일반 사용자는 본인이 업로드한 영상만 지울 수 있습니다.")
    public ResponseEntity<ApiResponse<String>> deleteVideoInPlaylist(
        @CurrentUser User user,
        @PathVariable String playlistCode,
        @PathVariable String videoCode
    ) {
        videoCommandService.deleteVideoFromPlaylist(user, playlistCode, videoCode);
        return ResponseEntity.ok(ApiResponse.onSuccess(playlistCode));
    }

    @PatchMapping("/{playlistCode}/{videoCode}/priority")
    @Operation(summary = "재생목록 영상 우선순위 변경", description = "재생목록 영상 우선순위 변경 기능입니다. \n어떤 플레이리스트(playlistCode)에, \n어떤 영상(videoCode)을 \n어떤 영상(targetVideoCode)의 \n전후(positionBefore)로 이동시킬지 결정")
    public ResponseEntity<ApiResponse<MoveVideoResponseDTO>> updateVideoPriority(
        @CurrentUser User user,
        @PathVariable String playlistCode,
        @PathVariable String videoCode,
        @RequestParam String targetVideoCode,
        @RequestParam Boolean positionBefore
    ) {

        return ResponseEntity.ok(ApiResponse.onSuccess(
            videoCommandService.moveVideo(user, playlistCode, videoCode, targetVideoCode,
                positionBefore)));
    }

}