package com.thedebugnaths.ai_mindmirror.dto.trugen;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TrugenTranscriptMessage(
        @JsonProperty("timestamp") String timestamp,
        @JsonProperty("role") String role,
        @JsonProperty("content") String content,
        @JsonProperty("type") String type,
        @JsonProperty("is_error") Boolean isError
) {}