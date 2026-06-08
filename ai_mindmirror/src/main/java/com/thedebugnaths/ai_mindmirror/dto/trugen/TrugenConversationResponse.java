package com.thedebugnaths.ai_mindmirror.dto.trugen;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record TrugenConversationResponse(
        @JsonProperty("id") String id,
        @JsonProperty("agent_id") String agentId,
        @JsonProperty("duration") Double duration,
        @JsonProperty("recording_url") String recordingUrl,
        @JsonProperty("status") String status,
        @JsonProperty("transcript") List<TrugenTranscriptMessage> transcript
) {}