package com.collabflow.document.controller;

import com.collabflow.auth.domain.User;
import com.collabflow.common.response.ApiResponse;
import com.collabflow.document.dto.DocumentCreateRequest;
import com.collabflow.document.dto.DocumentUpdateRequest;
import com.collabflow.document.dto.DocumentResponse;
import com.collabflow.document.dto.DocumentVersionResponse;
import com.collabflow.document.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Document", description = "문서 CRUD 및 버전 관리 API")
@RestController
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @Operation(summary = "워크스페이스 내 문서 목록 조회 (제목 검색 지원)")
    @GetMapping("/api/v1/workspaces/{workspaceId}/documents")
    public ApiResponse<List<DocumentResponse>> list(
            @PathVariable UUID workspaceId,
            @RequestParam(required = false) String search,
            @AuthenticationPrincipal User user) {
        return ApiResponse.ok(documentService.findAll(workspaceId, search, user.getId()));
    }

    @Operation(summary = "문서 생성")
    @PostMapping("/api/v1/workspaces/{workspaceId}/documents")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<DocumentResponse> create(@PathVariable UUID workspaceId,
                                                 @Valid @RequestBody DocumentCreateRequest request,
                                                 @AuthenticationPrincipal User user) {
        return ApiResponse.ok(documentService.create(workspaceId, request, user.getId()));
    }

    @Operation(summary = "문서 단건 조회")
    @GetMapping("/api/v1/documents/{id}")
    public ApiResponse<DocumentResponse> get(@PathVariable UUID id,
                                              @AuthenticationPrincipal User user) {
        return ApiResponse.ok(documentService.findById(id, user.getId()));
    }

    @Operation(summary = "문서 제목 수정")
    @PatchMapping("/api/v1/documents/{id}")
    public ApiResponse<DocumentResponse> update(@PathVariable UUID id,
                                                 @Valid @RequestBody DocumentUpdateRequest request,
                                                 @AuthenticationPrincipal User user) {
        return ApiResponse.ok(documentService.update(id, request, user.getId()));
    }

    @Operation(summary = "문서 삭제")
    @DeleteMapping("/api/v1/documents/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id, @AuthenticationPrincipal User user) {
        documentService.delete(id, user.getId());
    }

    @Operation(summary = "버전 이력 목록 조회")
    @GetMapping("/api/v1/documents/{id}/versions")
    public ApiResponse<List<DocumentVersionResponse>> versions(@PathVariable UUID id,
                                                                @AuthenticationPrincipal User user) {
        return ApiResponse.ok(documentService.getVersions(id, user.getId()));
    }

    @Operation(summary = "특정 버전으로 복원")
    @PostMapping("/api/v1/documents/{id}/restore/{versionId}")
    public ApiResponse<DocumentResponse> restore(@PathVariable UUID id,
                                                  @PathVariable UUID versionId,
                                                  @AuthenticationPrincipal User user) {
        return ApiResponse.ok(documentService.restoreVersion(id, versionId, user.getId()));
    }
}
