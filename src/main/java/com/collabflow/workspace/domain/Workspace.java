package com.collabflow.workspace.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "workspaces")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Workspace {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public static Workspace create(String name, UUID ownerId) {
        Workspace ws = new Workspace();
        ws.name = name;
        ws.ownerId = ownerId;
        return ws;
    }

    public void rename(String name) {
        this.name = name;
    }
}
