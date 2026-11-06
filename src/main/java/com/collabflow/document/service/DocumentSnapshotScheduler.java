package com.collabflow.document.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class DocumentSnapshotScheduler {

    private final CollaborationService collaborationService;

    /** 30초마다 변경된 문서를 PostgreSQL에 스냅샷 저장 */
    @Scheduled(fixedDelay = 30_000)
    public void saveSnapshots() {
        Set<String> dirtyDocIds = collaborationService.getDirtyDocIds();
        if (dirtyDocIds == null || dirtyDocIds.isEmpty()) return;

        log.debug("Saving snapshots for {} documents", dirtyDocIds.size());
        for (String docId : dirtyDocIds) {
            try {
                collaborationService.saveSnapshot(docId);
            } catch (Exception e) {
                log.error("Failed to save snapshot for document {}", docId, e);
            }
        }
    }
}
