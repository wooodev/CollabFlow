package com.collabflow.document.repository;

import com.collabflow.document.domain.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DocumentRepository extends JpaRepository<Document, UUID> {

    List<Document> findByWorkspaceIdOrderByUpdatedAtDesc(UUID workspaceId);

    List<Document> findByWorkspaceIdAndTitleContainingIgnoreCase(UUID workspaceId, String title);
}
