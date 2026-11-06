package com.collabflow.workspace.repository;

import com.collabflow.workspace.domain.WorkspaceMember;
import com.collabflow.workspace.domain.WorkspaceMember.WorkspaceMemberId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkspaceMemberRepository extends JpaRepository<WorkspaceMember, WorkspaceMemberId> {

    List<WorkspaceMember> findByIdWorkspaceId(UUID workspaceId);

    Optional<WorkspaceMember> findByIdWorkspaceIdAndIdUserId(UUID workspaceId, UUID userId);

    boolean existsByIdWorkspaceIdAndIdUserId(UUID workspaceId, UUID userId);

    void deleteByIdWorkspaceIdAndIdUserId(UUID workspaceId, UUID userId);
}
