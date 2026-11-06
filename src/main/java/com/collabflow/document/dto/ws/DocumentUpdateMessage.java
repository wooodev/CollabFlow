package com.collabflow.document.dto.ws;

import lombok.Getter;

@Getter
public class DocumentUpdateMessage {
    private String docId;
    /** Base64 인코딩된 Yjs update binary */
    private String update;
}
