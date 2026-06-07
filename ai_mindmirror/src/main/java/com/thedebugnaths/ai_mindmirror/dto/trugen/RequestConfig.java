package com.thedebugnaths.ai_mindmirror.dto.trugen;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public record RequestConfig(
        @JsonProperty("method") String method,
        @JsonProperty("url") String url,
        @JsonProperty("headers") Map<String, String> headers
) {}
