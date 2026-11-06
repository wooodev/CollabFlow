package com.collabflow.workspace.dto;

import com.collabflow.workspace.domain.Workspace;
import com.collabflow.workspace.domain.WorkspaceMember.MemberRole;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class WorkspaceResponse {

    private UUID id;
    private String name;
    private UUID ownerId;
    private MemberRole myRole;
    private LocalDateTime createdAt;

    public static WorkspaceResponse of(Workspace workspace, MemberRole myRole) {
        return WorkspaceResponse.builder()
                .id(workspace.getId())
                .name(workspace.getName())
                .ownerId(workspace.getOwnerId())
                .myRole(myRole)
                .createdAt(workspace.getCreatedAt())
                .build();
    }
}
