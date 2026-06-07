package com.thedebugnaths.ai_mindmirror.dto.trugen;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TrugenLifecycleRequest(
        @JsonProperty("timestamp") Double timestamp,
        @JsonProperty("conversation_id") String conversationId,
        @JsonProperty("type") String type,
        @JsonProperty("event") LifecycleEvent event
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record LifecycleEvent(
            @JsonProperty("name") String name,
            @JsonProperty("payload") Map<String, Object> payload
    ) {}
}