package com.collabflow.document.service;

import com.collabflow.auth.domain.User;
import com.collabflow.common.exception.BusinessException;
import com.collabflow.document.domain.Document;
import com.collabflow.document.dto.ws.BroadcastPayload;
import com.collabflow.document.dto.ws.DocumentInitPayload;
import com.collabflow.document.repository.DocumentRepository;
import com.collabflow.workspace.repository.WorkspaceMemberRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CollaborationService {

    private static final String DOC_STATE_KEY   = "doc:state:";
    private static final String DOC_ACTIVE_KEY  = "doc:active:";
    private static final String DOC_UPDATE_CHAN  = "doc:updates:";
    private static final String DOC_DIRTY_KEY   = "docs:dirty";

    private final DocumentRepository documentRepository;
    private final WorkspaceMemberRepository memberRepository;
    private final StringRedisTemplate stringRedisTemplate;
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    // ---------------------------------------------------------------
    // 클라이언트 → 서버
    // ---------------------------------------------------------------

    /** 문서 편집 세션 참여: 현재 상태를 개인 큐로 전송 */
    public DocumentInitPayload join(String docId, User user) {
        requireAccess(UUID.fromString(docId), user.getId());

        stringRedisTemplate.opsForSet().add(DOC_ACTIVE_KEY + docId, user.getId().toString());

        // 다른 편집자에게 입장 알림
        broadcastEvent(docId, BroadcastPayload.Type.USER_JOINED, user, null);

        String state = stringRedisTemplate.opsForValue().get(DOC_STATE_KEY + docId);
        List<String> activeUsers = getActiveUsers(docId);

        return DocumentInitPayload.builder()
                .docId(docId)
                .state(state)
                .activeUsers(activeUsers)
                .build();
    }

    /** 문서 편집 세션 이탈 */
    public void leave(String docId, User user) {
        stringRedisTemplate.opsForSet().remove(DOC_ACTIVE_KEY + docId, user.getId().toString());
        broadcastEvent(docId, BroadcastPayload.Type.USER_LEFT, user, null);
    }

    /**
     * Yjs update 적용:
     * 1. Redis에 update 누적 (state 갱신은 클라이언트 합의 기반 — 여기서는 최신 update를 덮어씀)
     * 2. Redis Pub/Sub으로 타 서버에 전파
     * 3. docs:dirty에 마킹 (스케줄러가 30초마다 DB 저장)
     */
    public void applyUpdate(String docId, String updateBase64, User user) {
        requireAccess(UUID.fromString(docId), user.getId());

        // 현재 Redis 상태 저장 (Phase 2에서는 마지막 update를 state로 사용)
        stringRedisTemplate.opsForValue().set(DOC_STATE_KEY + docId, updateBase64);
        stringRedisTemplate.opsForSet().add(DOC_DIRTY_KEY, docId);

        // Redis Pub/Sub으로 다른 서버에 전파
        BroadcastPayload payload = buildPayload(BroadcastPayload.Type.UPDATE, docId, user, updateBase64);
        publishToRedis(docId, payload);
    }

    /** 커서 위치 공유 */
    public void updateCursor(String docId, Object cursor, User user) {
        BroadcastPayload payload = buildPayload(BroadcastPayload.Type.CURSOR, docId, user, cursor);
        // 커서는 빠른 전파만 필요 — Pub/Sub 없이 로컬 STOMP 브로드캐스트
        messagingTemplate.convertAndSend("/topic/document." + docId, payload);
    }

    // ---------------------------------------------------------------
    // Redis Pub/Sub 수신 (다른 서버에서 발행된 update를 로컬 STOMP로 전달)
    // ---------------------------------------------------------------

    /** RedisMessageListenerContainer에서 호출 */
    public void onRedisMessage(String message, String channel) {
        try {
            BroadcastPayload payload = objectMapper.readValue(message, BroadcastPayload.class);
            String docId = channel.replace(DOC_UPDATE_CHAN, "");
            messagingTemplate.convertAndSend("/topic/document." + docId, payload);
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize Redis message on channel {}", channel, e);
        }
    }

    // ---------------------------------------------------------------
    // 스냅샷 (DocumentSnapshotScheduler에서 호출)
    // ---------------------------------------------------------------

    public Set<String> getDirtyDocIds() {
        return stringRedisTemplate.opsForSet().members(DOC_DIRTY_KEY);
    }

    public void saveSnapshot(String docId) {
        String state = stringRedisTemplate.opsForValue().get(DOC_STATE_KEY + docId);
        if (state == null) return;

        documentRepository.findById(UUID.fromString(docId)).ifPresent(doc -> {
            doc.updateContent(state);
            documentRepository.save(doc);
            stringRedisTemplate.opsForSet().remove(DOC_DIRTY_KEY, docId);
            log.debug("Snapshot saved for document {}", docId);
        });
    }

    // ---------------------------------------------------------------
    // 내부 helpers
    // ---------------------------------------------------------------

    private void requireAccess(UUID docId, UUID userId) {
        Document doc = documentRepository.findById(docId)
                .orElseThrow(() -> BusinessException.notFound("Document not found"));
        if (!memberRepository.existsByIdWorkspaceIdAndIdUserId(doc.getWorkspaceId(), userId)) {
            throw BusinessException.forbidden("Not a member of this workspace");
        }
    }

    private List<String> getActiveUsers(String docId) {
        Set<String> members = stringRedisTemplate.opsForSet().members(DOC_ACTIVE_KEY + docId);
        return members == null ? List.of() : members.stream().toList();
    }

    private void broadcastEvent(String docId, BroadcastPayload.Type type, User user, Object data) {
        BroadcastPayload payload = buildPayload(type, docId, user, data);
        messagingTemplate.convertAndSend("/topic/document." + docId, payload);
    }

    private BroadcastPayload buildPayload(BroadcastPayload.Type type, String docId, User user, Object data) {
        return BroadcastPayload.builder()
                .type(type)
                .docId(docId)
                .userId(user.getId().toString())
                .userName(user.getName())
                .data(data)
                .build();
    }

    private void publishToRedis(String docId, BroadcastPayload payload) {
        try {
            String json = objectMapper.writeValueAsString(payload);
            stringRedisTemplate.convertAndSend(DOC_UPDATE_CHAN + docId, json);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize broadcast payload", e);
        }
    }
}
