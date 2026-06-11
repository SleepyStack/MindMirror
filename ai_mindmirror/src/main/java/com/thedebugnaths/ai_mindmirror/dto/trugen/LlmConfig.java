package com.thedebugnaths.ai_mindmirror.dto.trugen;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record LlmConfig(
        String model,
        String provider,
        @JsonProperty("url") String baseUrl,
        @JsonProperty("token") String apiKey
) {
    // Overloaded constructor for STANDARD providers (English)
    public LlmConfig(String model, String provider) {
        this(model, provider, null, null);
    }
}