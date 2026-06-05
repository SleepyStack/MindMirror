package com.thedebugnaths.ai_mindmirror.dto;

public record TrugenWebhookRequest(
        String actionStep,
        String emotionEnd,
        String emotionStart,
        String mainTopic,
        String summary
) {}