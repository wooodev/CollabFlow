package com.collabflow.document.dto;

import com.collabflow.document.domain.DocumentVersion;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class DocumentVersionResponse {

    private UUID id;
    private UUID documentId;
    private UUID createdBy;
    private LocalDateTime createdAt;

    public static DocumentVersionResponse from(DocumentVersion version) {
        return DocumentVersionResponse.builder()
                .id(version.getId())
                .documentId(version.getDocumentId())
                .createdBy(version.getCreatedBy())
                .createdAt(version.getCreatedAt())
                .build();
    }
}
