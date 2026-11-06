package com.collabflow.document.controller;

import com.collabflow.auth.domain.User;
import com.collabflow.document.dto.ws.*;
import com.collabflow.document.service.CollaborationService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class DocumentWebSocketController {

    private final CollaborationService collaborationService;

    /**
     * 클라이언트: STOMP SEND /app/document.join
     * 서버 → 개인: /user/queue/document.init (현재 문서 상태)
     */
    @MessageMapping("document.join")
    @SendToUser("/queue/document.init")
    public DocumentInitPayload join(DocumentJoinMessage message,
                                    @AuthenticationPrincipal User user) {
        return collaborationService.join(message.getDocId(), user);
    }

    /**
     * 클라이언트: STOMP SEND /app/document.leave
     */
    @MessageMapping("document.leave")
    public void leave(DocumentLeaveMessage message,
                      @AuthenticationPrincipal User user) {
        collaborationService.leave(message.getDocId(), user);
    }

    /**
     * 클라이언트: STOMP SEND /app/document.update
     * → Redis 상태 갱신 → Redis Pub/Sub → /topic/document.{docId} 브로드캐스트
     */
    @MessageMapping("document.update")
    public void update(DocumentUpdateMessage message,
                       @AuthenticationPrincipal User user) {
        collaborationService.applyUpdate(message.getDocId(), message.getUpdate(), user);
    }

    /**
     * 클라이언트: STOMP SEND /app/document.cursor
     * → /topic/document.{docId} 브로드캐스트 (커서 위치 공유)
     */
    @MessageMapping("document.cursor")
    public void cursor(DocumentCursorMessage message,
                       @AuthenticationPrincipal User user) {
        collaborationService.updateCursor(message.getDocId(), message.getCursor(), user);
    }
}
