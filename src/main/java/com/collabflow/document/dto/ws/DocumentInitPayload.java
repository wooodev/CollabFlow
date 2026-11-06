package com.collabflow.document.dto.ws;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/** 최초 접속 시 클라이언트에게 전달하는 현재 문서 상태 */
@Getter
@Builder
public class DocumentInitPayload {
    private String docId;
    /** 현재 Yjs 문서 스냅샷 (Base64) — null이면 새 문서 */
    private String state;
    /** 현재 편집 중인 사용자 ID 목록 */
    private List<String> activeUsers;
}
