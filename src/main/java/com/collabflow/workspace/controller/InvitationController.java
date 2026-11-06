package com.collabflow.workspace.controller;

import com.collabflow.auth.domain.User;
import com.collabflow.common.response.ApiResponse;
import com.collabflow.workspace.dto.InvitationResponse;
import com.collabflow.workspace.service.InvitationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Invitation", description = "초대 수락 API")
@RestController
@RequestMapping("/api/v1/invitations")
@RequiredArgsConstructor
public class InvitationController {

    private final InvitationService invitationService;

    @Operation(summary = "초대 정보 조회 (비로그인 접근 가능)")
    @GetMapping("/{token}")
    public ApiResponse<InvitationResponse> get(@PathVariable UUID token) {
        return ApiResponse.ok(invitationService.getInvitation(token));
    }

    @Operation(summary = "초대 수락")
    @PostMapping("/{token}/accept")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void accept(@PathVariable UUID token,
                       @AuthenticationPrincipal User user) {
        invitationService.accept(token, user.getId());
    }
}
