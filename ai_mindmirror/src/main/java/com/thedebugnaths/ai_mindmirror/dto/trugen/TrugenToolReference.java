package com.thedebugnaths.ai_mindmirror.dto.trugen;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TrugenToolReference(
        @JsonProperty("id") String id,
        @JsonProperty("name") String name
) {}