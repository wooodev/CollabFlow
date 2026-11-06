package com.collabflow.workspace.repository;

import com.collabflow.workspace.domain.Invitation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface InvitationRepository extends JpaRepository<Invitation, UUID> {

    Optional<Invitation> findByToken(UUID token);

    boolean existsByWorkspaceIdAndEmailAndAcceptedAtIsNull(UUID workspaceId, String email);
}
