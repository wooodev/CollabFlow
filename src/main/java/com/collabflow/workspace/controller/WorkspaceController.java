package com.collabflow.workspace.controller;

import com.collabflow.auth.domain.User;
import com.collabflow.common.response.ApiResponse;
import com.collabflow.workspace.dto.InviteMemberRequest;
import com.collabflow.workspace.dto.WorkspaceCreateRequest;
import com.collabflow.workspace.dto.WorkspaceResponse;
import com.collabflow.workspace.service.WorkspaceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/workspaces")
@RequiredArgsConstructor
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    @GetMapping
    public ApiResponse<List<WorkspaceResponse>> list(@AuthenticationPrincipal User user) {
        return ApiResponse.ok(workspaceService.findAll(user.getId()));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<WorkspaceResponse> create(@Valid @RequestBody WorkspaceCreateRequest request,
                                                  @AuthenticationPrincipal User user) {
        return ApiResponse.ok(workspaceService.create(request, user.getId()));
    }

    @GetMapping("/{id}")
    public ApiResponse<WorkspaceResponse> get(@PathVariable UUID id,
                                               @AuthenticationPrincipal User user) {
        return ApiResponse.ok(workspaceService.findById(id, user.getId()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id, @AuthenticationPrincipal User user) {
        workspaceService.delete(id, user.getId());
    }

    @PostMapping("/{id}/invite")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void invite(@PathVariable UUID id,
                       @Valid @RequestBody InviteMemberRequest request,
                       @AuthenticationPrincipal User user) {
        workspaceService.invite(id, request, user.getId());
    }

    @DeleteMapping("/{id}/members/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeMember(@PathVariable UUID id,
                              @PathVariable UUID userId,
                              @AuthenticationPrincipal User user) {
        workspaceService.removeMember(id, userId, user.getId());
    }
}
