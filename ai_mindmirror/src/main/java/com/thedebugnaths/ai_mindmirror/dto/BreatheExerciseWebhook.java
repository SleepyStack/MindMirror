package com.thedebugnaths.ai_mindmirror.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public record BreatheExerciseWebhook(
        @JsonProperty("call_id") String callId,
        @JsonProperty("name") String name,
        @JsonProperty("parameters") Map<String, Object> parameters
) {
}
