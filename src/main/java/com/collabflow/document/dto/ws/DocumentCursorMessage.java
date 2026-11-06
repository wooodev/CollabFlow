package com.collabflow.document.dto.ws;

import lombok.Getter;

@Getter
public class DocumentCursorMessage {
    private String docId;
    /** 커서 위치 정보 (anchor, head 등 Yjs RelativePosition JSON) */
    private Object cursor;
}
