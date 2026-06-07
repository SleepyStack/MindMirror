package com.thedebugnaths.ai_mindmirror.dto.trugen;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public record ToolSchema(
        @JsonProperty("type") String type,
        @JsonProperty("name") String name,
        @JsonProperty("description") String description,
        @JsonProperty("parameters") Map<String, Object> parameters
) {}
