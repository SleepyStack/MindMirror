package com.thedebugnaths.ai_mindmirror.dto.trugen;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record TrugenAgentRequest(
        @JsonProperty("agent_name") String name,
        @JsonProperty("agent_system_prompt") String systemPrompt,
        @JsonProperty("config") AgentConfig agentConfig,
        @JsonProperty("knowledge_base") List<KnowledgeBaseRef> knowledgeBases,
        @JsonProperty("record") boolean recording,
        @JsonProperty("callback_url") String webhookUrl,
        @JsonProperty("callback_events") List<String> webhookEvents,
        @JsonProperty("avatars") List<AvatarConfig> avatars,

        @JsonProperty("tool")
        List<TrugenToolReference> tools
) {}

