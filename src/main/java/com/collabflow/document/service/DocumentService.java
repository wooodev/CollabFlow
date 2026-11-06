package com.collabflow.document.service;

import com.collabflow.common.exception.BusinessException;
import com.collabflow.document.domain.Document;
import com.collabflow.document.domain.DocumentVersion;
import com.collabflow.document.dto.DocumentCreateRequest;
import com.collabflow.document.dto.DocumentUpdateRequest;
import com.collabflow.document.dto.DocumentResponse;
import com.collabflow.document.dto.DocumentVersionResponse;
import com.collabflow.document.repository.DocumentRepository;
import com.collabflow.document.repository.DocumentVersionRepository;
import com.collabflow.workspace.repository.WorkspaceMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentVersionRepository versionRepository;
    private final WorkspaceMemberRepository memberRepository;

    @Transactional
    public DocumentResponse create(UUID workspaceId, DocumentCreateRequest request, UUID userId) {
        requireMember(workspaceId, userId);
        Document doc = Document.create(workspaceId, request.getTitle(), userId);
        return DocumentResponse.from(documentRepository.save(doc));
    }

    @Transactional(readOnly = true)
    public List<DocumentResponse> findAll(UUID workspaceId, String search, UUID userId) {
        requireMember(workspaceId, userId);
        var docs = (search != null && !search.isBlank())
                ? documentRepository.findByWorkspaceIdAndTitleContainingIgnoreCase(workspaceId, search.trim())
                : documentRepository.findByWorkspaceIdOrderByUpdatedAtDesc(workspaceId);
        return docs.stream().map(DocumentResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public DocumentResponse findById(UUID docId, UUID userId) {
        Document doc = getDocumentOrThrow(docId);
        requireMember(doc.getWorkspaceId(), userId);
        return DocumentResponse.from(doc);
    }

    @Transactional
    public DocumentResponse update(UUID docId, DocumentUpdateRequest request, UUID userId) {
        Document doc = getDocumentOrThrow(docId);
        requireMember(doc.getWorkspaceId(), userId);
        if (request.getTitle() != null) {
            doc.updateTitle(request.getTitle());
        }
        return DocumentResponse.from(doc);
    }

    @Transactional
    public void delete(UUID docId, UUID userId) {
        Document doc = getDocumentOrThrow(docId);
        requireMember(doc.getWorkspaceId(), userId);
        documentRepository.delete(doc);
    }

    @Transactional(readOnly = true)
    public List<DocumentVersionResponse> getVersions(UUID docId, UUID userId) {
        Document doc = getDocumentOrThrow(docId);
        requireMember(doc.getWorkspaceId(), userId);
        return versionRepository.findByDocumentIdOrderByCreatedAtDesc(docId)
                .stream().map(DocumentVersionResponse::from).toList();
    }

    @Transactional
    public DocumentResponse restoreVersion(UUID docId, UUID versionId, UUID userId) {
        Document doc = getDocumentOrThrow(docId);
        requireMember(doc.getWorkspaceId(), userId);

        DocumentVersion version = versionRepository.findById(versionId)
                .orElseThrow(() -> BusinessException.notFound("Version not found"));

        // 복원 전 현재 상태를 새 버전으로 저장
        if (doc.getContent() != null) {
            versionRepository.save(DocumentVersion.snapshot(docId, doc.getContent(), userId));
        }
        doc.updateContent(version.getContent());
        return DocumentResponse.from(doc);
    }

    // --- helpers ---

    private Document getDocumentOrThrow(UUID docId) {
        return documentRepository.findById(docId)
                .orElseThrow(() -> BusinessException.notFound("Document not found"));
    }

    private void requireMember(UUID workspaceId, UUID userId) {
        if (!memberRepository.existsByIdWorkspaceIdAndIdUserId(workspaceId, userId)) {
            throw BusinessException.forbidden("Not a member of this workspace");
        }
    }
}
