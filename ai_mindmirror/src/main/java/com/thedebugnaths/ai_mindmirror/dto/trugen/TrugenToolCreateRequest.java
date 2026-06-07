package com.thedebugnaths.ai_mindmirror.dto.trugen;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public record TrugenToolCreateRequest(
        @JsonProperty("type") String type,
        @JsonProperty("schema") ToolSchema schema,
        @JsonProperty("request_config") RequestConfig requestConfig,
        @JsonProperty("event_messages") Map<String, Object> eventMessages
) {}
