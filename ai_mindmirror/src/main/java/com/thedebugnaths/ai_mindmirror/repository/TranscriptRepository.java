package com.thedebugnaths.ai_mindmirror.repository;

import com.thedebugnaths.ai_mindmirror.entity.SyncStatus;
import com.thedebugnaths.ai_mindmirror.entity.Transcript;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TranscriptRepository extends JpaRepository<Transcript, String> {
    Optional<Transcript> findByConversationId(String conversationId);
    List<Transcript> findBySyncStatus(SyncStatus syncStatus);
    List<Transcript> findAllByUserIdAndSyncStatusOrderByCreatedAtDesc(Long userId, SyncStatus syncStatus);
    List<Transcript> findTop3ByUserIdAndSyncStatusOrderByCreatedAtDesc(Long userId, SyncStatus syncStatus);
}
