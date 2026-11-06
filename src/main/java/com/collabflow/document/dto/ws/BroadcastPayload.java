package com.collabflow.document.dto.ws;

import lombok.Builder;
import lombok.Getter;

/**
 * /topic/document.{docId} 로 브로드캐스트되는 통합 페이로드.
 * type 필드로 클라이언트가 분기 처리.
 */
@Getter
@Builder
public class BroadcastPayload {

    public enum Type { UPDATE, CURSOR, USER_JOINED, USER_LEFT }

    private Type type;
    private String docId;
    private String userId;
    private String userName;
    /** UPDATE: Base64 Yjs update / CURSOR: cursor JSON / USER_JOINED|LEFT: null */
    private Object data;
}
