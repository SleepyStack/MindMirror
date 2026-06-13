package com.thedebugnaths.ai_mindmirror.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ChangeEmotionWebhook(
        @JsonProperty("emotion") String emotion
) {}