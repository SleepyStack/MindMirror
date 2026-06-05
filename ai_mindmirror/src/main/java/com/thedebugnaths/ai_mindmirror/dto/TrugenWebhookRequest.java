package com.thedebugnaths.ai_mindmirror.dto;

public record TrugenWebhookRequest(
        Long userId, // The AI will provide this!
        String actionStep,
        String emotionEnd,
        String emotionStart,
        String mainTopic,
        String summary
) {}