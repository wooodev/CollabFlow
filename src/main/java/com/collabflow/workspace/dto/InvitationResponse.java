package com.collabflow.workspace.dto;

import com.collabflow.workspace.domain.Invitation;
import com.collabflow.workspace.domain.WorkspaceMember.MemberRole;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class InvitationResponse {

    private UUID id;
    private UUID token;
    private UUID workspaceId;
    private String email;
    private MemberRole role;
    private LocalDateTime expiresAt;
    private boolean accepted;

    public static InvitationResponse from(Invitation invitation) {
        return InvitationResponse.builder()
                .id(invitation.getId())
                .token(invitation.getToken())
                .workspaceId(invitation.getWorkspaceId())
                .email(invitation.getEmail())
                .role(invitation.getRole())
                .expiresAt(invitation.getExpiresAt())
                .accepted(invitation.isAccepted())
                .build();
    }
}
