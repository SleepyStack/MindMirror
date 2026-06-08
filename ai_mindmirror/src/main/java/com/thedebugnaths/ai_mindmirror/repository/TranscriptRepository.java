package com.thedebugnaths.ai_mindmirror.repository;

import com.thedebugnaths.ai_mindmirror.entity.Transcript;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TranscriptRepository extends JpaRepository<Transcript, String> {
    Optional<Transcript> findByConversationId(String conversationId);
}
