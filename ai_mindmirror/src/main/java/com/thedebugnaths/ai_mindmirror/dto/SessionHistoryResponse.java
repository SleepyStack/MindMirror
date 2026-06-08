package com.thedebugnaths.ai_mindmirror.dto;

import com.thedebugnaths.ai_mindmirror.entity.SessionHistory;
import java.time.LocalDateTime;

public record SessionHistoryResponse(
        Long id,
        String agentId,
        String conversationId,
        String status,
        String mainTopic,
        String summaryText,
        String emotionStart,
        String emotionEnd,
        String actionStep,
        LocalDateTime createdAt,
        String username,
        String email
) {
    public static SessionHistoryResponse fromEntity(SessionHistory session) {
        return new SessionHistoryResponse(
                session.getId(),
                session.getAgentId(),
                session.getConversationId(),
                session.getStatus(),
                session.getMainTopic(),
                session.getSummaryText(),
                session.getEmotionStart(),
                session.getEmotionEnd(),
                session.getActionStep(),
                session.getCreatedAt(),
                session.getUser() != null ? session.getUser().getUsername() : null,
                session.getUser() != null ? session.getUser().getEmail() : null
        );
    }
}