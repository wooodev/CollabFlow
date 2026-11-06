package com.collabflow.workspace.controller;

import com.collabflow.auth.domain.User;
import com.collabflow.common.response.ApiResponse;
import com.collabflow.workspace.dto.InviteMemberRequest;
import com.collabflow.workspace.dto.InvitationResponse;
import com.collabflow.workspace.dto.WorkspaceCreateRequest;
import com.collabflow.workspace.dto.WorkspaceResponse;
import com.collabflow.workspace.service.InvitationService;
import com.collabflow.workspace.service.WorkspaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Workspace", description = "워크스페이스 관리 API")
@RestController
@RequestMapping("/api/v1/workspaces")
@RequiredArgsConstructor
public class WorkspaceController {

    private final WorkspaceService workspaceService;
    private final InvitationService invitationService;

    @Operation(summary = "내 워크스페이스 목록 조회")
    @GetMapping
    public ApiResponse<List<WorkspaceResponse>> list(@AuthenticationPrincipal User user) {
        return ApiResponse.ok(workspaceService.findAll(user.getId()));
    }

    @Operation(summary = "워크스페이스 생성")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<WorkspaceResponse> create(@Valid @RequestBody WorkspaceCreateRequest request,
                                                  @AuthenticationPrincipal User user) {
        return ApiResponse.ok(workspaceService.create(request, user.getId()));
    }

    @Operation(summary = "워크스페이스 상세 조회")
    @GetMapping("/{id}")
    public ApiResponse<WorkspaceResponse> get(@PathVariable UUID id,
                                               @AuthenticationPrincipal User user) {
        return ApiResponse.ok(workspaceService.findById(id, user.getId()));
    }

    @Operation(summary = "워크스페이스 삭제 (Owner 전용)")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id, @AuthenticationPrincipal User user) {
        workspaceService.delete(id, user.getId());
    }

    @Operation(summary = "멤버 이메일 초대 (이메일 발송)")
    @PostMapping("/{id}/invite")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<InvitationResponse> invite(@PathVariable UUID id,
                                                   @Valid @RequestBody InviteMemberRequest request,
                                                   @AuthenticationPrincipal User user) {
        return ApiResponse.ok(invitationService.invite(id, request, user.getId()));
    }

    @Operation(summary = "멤버 제거 (Owner 전용)")
    @DeleteMapping("/{id}/members/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeMember(@PathVariable UUID id,
                              @PathVariable UUID userId,
                              @AuthenticationPrincipal User user) {
        workspaceService.removeMember(id, userId, user.getId());
    }
}
