package com.collabflow.document.dto;

import com.collabflow.document.domain.Document;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class DocumentResponse {

    private UUID id;
    private UUID workspaceId;
    private String title;
    private UUID createdBy;
    private UUID folderId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static DocumentResponse from(Document doc) {
        return DocumentResponse.builder()
                .id(doc.getId())
                .workspaceId(doc.getWorkspaceId())
                .title(doc.getTitle())
                .createdBy(doc.getCreatedBy())
                .folderId(doc.getFolderId())
                .createdAt(doc.getCreatedAt())
                .updatedAt(doc.getUpdatedAt())
                .build();
    }
}
