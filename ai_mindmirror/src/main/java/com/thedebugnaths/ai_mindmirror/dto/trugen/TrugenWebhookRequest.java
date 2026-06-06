package com.thedebugnaths.ai_mindmirror.dto.trugen;

public record TrugenWebhookRequest(
        String summary,
        String mainTopic,
        String emotionStart,
        String emotionEnd,
        String actionStep
) {}
