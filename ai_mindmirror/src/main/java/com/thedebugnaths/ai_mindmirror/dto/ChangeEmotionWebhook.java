package com.thedebugnaths.ai_mindmirror.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ChangeEmotionWebhook(
        @JsonProperty("call_id") String callId,
        @JsonProperty("name") String name,
        @JsonProperty("parameters") EmotionParameters parameters
) {
    public record EmotionParameters(
            @JsonProperty("emotion") String emotion
    ) {}
}
