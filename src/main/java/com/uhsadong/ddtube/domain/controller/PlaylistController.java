package com.uhsadong.ddtube.domain.controller;

import com.uhsadong.ddtube.domain.dto.request.CreatePlaylistRequestDTO;
import com.uhsadong.ddtube.domain.dto.response.CreatePlaylistResponseDTO;
import com.uhsadong.ddtube.domain.dto.response.PlaylistDetailResponseDTO;
import com.uhsadong.ddtube.domain.dto.response.PlaylistMetaResponseDTO;
import com.uhsadong.ddtube.domain.dto.response.PlaylistPublicMetaResponseDTO;
import com.uhsadong.ddtube.domain.entity.User;
import com.uhsadong.ddtube.domain.service.PlaylistCommandService;
import com.uhsadong.ddtube.domain.service.PlaylistQueryService;
import com.uhsadong.ddtube.global.response.ApiResponse;
import com.uhsadong.ddtube.global.security.CurrentUser;
import com.uhsadong.ddtube.global.util.S3Util;
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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/playlist")
public class PlaylistController {

    private final PlaylistCommandService playlistCommandService;
    private final S3Util s3Util;
    private final PlaylistQueryService playlistQueryService;

    @GetMapping("/{playlistCode}")
    @Operation(summary = "재생목록 조회", description = "재생목록 조회 기능입니다.")
    public ResponseEntity<ApiResponse<PlaylistDetailResponseDTO>> getPlaylist(
        @CurrentUser User user,
        @PathVariable String playlistCode
    ) {
        return ResponseEntity.ok(ApiResponse.onSuccess(
            playlistQueryService.getPlaylistDetail(user, playlistCode)
        ));
    }

    @GetMapping("/meta/{playlistCode}/public")
    @Operation(summary = "재생목록 메타정보 조회 (JWT 불필요)", description = "초대장에서 사용할 메타정보 조회 기능입니다. 초대장 이름, 썸네일 주소, 초대장 설명을 반환합니다.")
    public ResponseEntity<ApiResponse<PlaylistPublicMetaResponseDTO>> getPlaylistPublicMeta(
        @PathVariable String playlistCode
    ) {
        return ResponseEntity.ok(ApiResponse.onSuccess(
            playlistQueryService.getPlaylistPublicMetaInformation(playlistCode)
        ));
    }

    @GetMapping("/meta/{playlistCode}")
    @Operation(summary = "재생목록 메타정보 조회", description = "초대장에서 사용할 메타정보 조회 기능입니다. 초대장 이름, 썸네일 주소, 관리자, 참여자 목록을 반환합니다.")
    public ResponseEntity<ApiResponse<PlaylistMetaResponseDTO>> getPlaylistMeta(
        @CurrentUser User user,
        @PathVariable String playlistCode
    ) {
        return ResponseEntity.ok(ApiResponse.onSuccess(
            playlistQueryService.getPlaylistMetaInformation(user, playlistCode)
        ));
    }

    @PostMapping()
    @Operation(summary = "재생목록 생성", description = "재생목록을 생성합니다. 생성하는 유저는 필수적으로 name과 password를 입력해야합니다. \nJwt를 반환합니다. \n썸네일 주소는 선택사항으로, 빈 공백을 전송했을 때 default 이미지로 적용됩니다. \n이미지 업로드는 /playlist/thumbnail로 가능합니다. \n해당 썸네일 주소는 S3 파일 경로로 필터링되며 이상한 주소가 입력되었을 때에는 에러를 반환합니다.")
    public ResponseEntity<ApiResponse<CreatePlaylistResponseDTO>> createPlaylist(
        @RequestBody @Valid CreatePlaylistRequestDTO createPlaylistRequestDTO
    ) {
        return ResponseEntity.ok(ApiResponse.onSuccess(
            playlistCommandService.createPlaylist(createPlaylistRequestDTO)
        ));
    }

    @DeleteMapping("/{playlistCode}")
    @Operation(summary = "재생목록 삭제", description = "재생목록 삭제 기능입니다. 관리자만 삭제 가능합니다.")
    public ResponseEntity<ApiResponse<String>> deletePlaylist(
        @CurrentUser User user,
        @PathVariable String playlistCode
    ) {
        playlistCommandService.deletePlaylist(user, playlistCode);
        return ResponseEntity.ok(ApiResponse.onSuccess("삭제 완료"));
    }

    @PostMapping("/thumbnail")
    @Operation(summary = "썸네일 업로드", description = "재생목록을 만들 때 썸네일을 업로드합니다. 여기서 반환되는 전체 url을 재생목록을 생성할 때 넣어주시면 됩니다.")
    public ResponseEntity<ApiResponse<String>> uploadToS3(
        @RequestPart(value = "file") MultipartFile file
    ) {
        return ResponseEntity.ok(ApiResponse.onSuccess(
            s3Util.upload(file)
        ));
    }

    @PostMapping("/{playlistCode}/now-playing")
    @Operation(summary = "현재 재생 중인 영상 설정", description = "현재 재생중인 영상을 바꿉니다. TODO: 관리자가 아니라면 현재 재생중인 영상보다 우선순위가 다음으로 높은 영상으로만 바꿀 수 있습니다.")
    public ResponseEntity<ApiResponse<String>> setNowPlaying(
        @CurrentUser User user,
        @PathVariable String playlistCode,
        @RequestParam String videoCode
    ) {
        playlistCommandService.setNowPlayingVideo(user, playlistCode, videoCode);
        return ResponseEntity.ok(ApiResponse.onSuccess("변경 완료"));
    }
}
