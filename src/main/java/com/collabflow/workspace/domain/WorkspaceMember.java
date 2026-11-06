package com.collabflow.workspace.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "workspace_members")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WorkspaceMember {

    @EmbeddedId
    private WorkspaceMemberId id;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private MemberRole role;

    @CreatedDate
    @Column(name = "joined_at", updatable = false)
    private LocalDateTime joinedAt;

    public static WorkspaceMember of(UUID workspaceId, UUID userId, MemberRole role) {
        WorkspaceMember member = new WorkspaceMember();
        member.id = new WorkspaceMemberId(workspaceId, userId);
        member.role = role;
        return member;
    }

    public enum MemberRole {
        OWNER, EDITOR, VIEWER
    }

    @Embeddable
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class WorkspaceMemberId implements java.io.Serializable {
        @Column(name = "workspace_id")
        private UUID workspaceId;

        @Column(name = "user_id")
        private UUID userId;
    }
}
