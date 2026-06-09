package com.thedebugnaths.ai_mindmirror.entity;

import com.thedebugnaths.ai_mindmirror.dto.trugen.TrugenConversationResponse;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.Instant;

@Entity
@Table(name = "transcripts")
@NoArgsConstructor
@Getter
public class Transcript {

    @Id
    @Column(name = "conversation_id", nullable = false)
    private String conversationId;

    // Completely decoupled from the User entity. Just a raw foreign key reference now.
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", columnDefinition = "jsonb")
    private TrugenConversationResponse payload;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt = Instant.now();

    @Enumerated(EnumType.STRING)
    @Column(name = "sync_status", nullable = false)
    private SyncStatus syncStatus = SyncStatus.PENDING;

    public Transcript(String conversationId, Long userId, TrugenConversationResponse payload, SyncStatus syncStatus) {
        this.conversationId = conversationId;
        this.userId = userId;
        this.payload = payload;
        this.syncStatus = syncStatus;
    }
    public void setPayload(TrugenConversationResponse payload) {
        this.payload = payload;
    }
    public void setSyncStatus(SyncStatus syncStatus) {
        this.syncStatus = syncStatus;
    }
}