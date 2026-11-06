package com.collabflow.workspace.repository;

import com.collabflow.workspace.domain.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface WorkspaceRepository extends JpaRepository<Workspace, UUID> {

    @Query("""
            SELECT w FROM Workspace w
            JOIN WorkspaceMember m ON m.id.workspaceId = w.id
            WHERE m.id.userId = :userId
            """)
    List<Workspace> findAllByMemberUserId(@Param("userId") UUID userId);
}
