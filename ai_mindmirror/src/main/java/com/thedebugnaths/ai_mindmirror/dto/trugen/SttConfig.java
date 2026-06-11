package com.thedebugnaths.ai_mindmirror.dto.trugen;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record SttConfig(
        @JsonProperty("model") String model,
        @JsonProperty("provider") String provider,
        @JsonProperty("language") String language,
        @JsonProperty("min_endpointing_delay") double minEndpointingDelay,
        @JsonProperty("max_endpointing_delay") double maxEndpointingDelay
) {
    // Overloaded constructor for English/Default models that do not require an explicit language key
    public SttConfig(String model, String provider, double minEndpointingDelay, double maxEndpointingDelay) {
        this(model, provider, null, minEndpointingDelay, maxEndpointingDelay);
    }
}