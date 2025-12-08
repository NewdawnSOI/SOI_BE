package com.soi.backend.domain.friend.controller;

import com.soi.backend.domain.friend.dto.*;
import com.soi.backend.domain.friend.entity.FriendStatus;
import com.soi.backend.domain.friend.service.FriendService;
import com.soi.backend.global.ApiResponseDto;
import com.soi.backend.domain.user.dto.UserFindRespDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/friend")

@Tag(name = "friend API", description = "친구 관리 API")
public class FriendController {

    private final FriendService friendService;

    @Operation(summary = "친구 추가", description = "사용자 id를 통해 친구추가를 합니다.")
    @PostMapping("/create")
    public ResponseEntity<ApiResponseDto<FriendRespDto>> create(@RequestBody FriendCreateReqDto friendCreateReqDto) {
        FriendRespDto friendRespDto = friendService.createFriendRequest(friendCreateReqDto);
        return ResponseEntity.ok(ApiResponseDto.success(friendRespDto,"친구 요청 성공"));
    }

    @Operation(summary = "친구 상태 업데이트", description = "친구 관계 id, 상태 : ACCEPTED, CANCELLED 를 받아 상태를 업데이트합니다.")
    @PostMapping("/update")
    public ResponseEntity<ApiResponseDto<FriendRespDto>> update(@RequestBody FriendUpdateRespDto friendUpdateRespDto) {
        FriendRespDto friendRespDto = friendService.updateFriendRequest(friendUpdateRespDto);
        return ResponseEntity.ok(ApiResponseDto.success(friendRespDto, "친구 상태 업데이트 성공"));
    }

    @Operation(summary = "친구 삭제", description = "삭제 요청을 한 사용자의 id : requesterId에\n" +
            "삭제를 당하는 사용자의 id : receiverId에 담아서 요청\n" +
            "만약 삭제후, 서로가 삭제된 관계면 친구 관계 컬럼을 삭제함" )
    @PostMapping("/delete")
    public ResponseEntity<ApiResponseDto<Boolean>> deleteFriend(@RequestBody FriendReqDto friendReqDto) {
        Boolean deleteStatus = friendService.deleteFriend(friendReqDto);
        return ResponseEntity.ok(ApiResponseDto.success(deleteStatus, "친구 삭제 성공"));
    }

    @Operation(summary = "모든 친구 조회", description = "유저의 id (user_id 말고 그냥 id)를 통해 모든 친구를 조회합니다.")
    @GetMapping("/get-all")
    public ResponseEntity<ApiResponseDto<List<UserFindRespDto>>> getAllFriend(@RequestParam Long id, @RequestParam FriendStatus friendStatus) {
        List<UserFindRespDto> friends = friendService.getAllFriends(id, friendStatus);
        return ResponseEntity.ok(ApiResponseDto.success(friends, "모든 친구 조회 완료"));
    }

    @Operation(summary = "연락처에 있는 친구들 관계확인", description = "유저의 id와 연락처에 있는 친구들 전화번호를 List로 받아서 관계를 리턴합니다.")
    @GetMapping("/check-friend-relation")
    public ResponseEntity<ApiResponseDto<List<FriendCheckRespDto>>> getAllFriend(@RequestParam Long id, @RequestParam List<String> friendPhoneNums) {
        List<FriendCheckRespDto> friends = friendService.checkIsFriend(id,friendPhoneNums);
        return ResponseEntity.ok(ApiResponseDto.success(friends, "모든 친구 조회 완료"));
    }

    @Operation(summary = "친구 차단", description = "차단 요청을 한 사용자의 id : requesterId에\n" +
            "차단을 당하는 사용자의 id : receiverId에 담아서 요청")
    @PostMapping("/block")
    public ResponseEntity<ApiResponseDto<Boolean>> blockFriend(@RequestBody FriendReqDto friendReqDto) {
        Boolean deleteStatus = friendService.blockFriend(friendReqDto);
        return ResponseEntity.ok(ApiResponseDto.success(deleteStatus, "친구 차단 성공"));
    }

    @Operation(summary = "친구 차단 해제", description = "차단 해제 요청을 한 사용자의 id : requesterId에\n" +
            "차단 해제를 당하는 사용자의 id : receiverId에 담아서 요청" +
            "차단 해제후에는 친구 관계가 완전 초기화 (삭제) 됩니다.")
    @PostMapping("/unblock")
    public ResponseEntity<ApiResponseDto<Boolean>> unBlockFriend(@RequestBody FriendReqDto friendReqDto) {
        Boolean deleteStatus = friendService.unBlockFriend(friendReqDto);
        return ResponseEntity.ok(ApiResponseDto.success(deleteStatus, "친구 차단 해제 성공"));
    }
}
