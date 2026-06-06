package com.thedebugnaths.ai_mindmirror.dto.trugen;

import java.util.List;

public record TrugenAgentRequest(
        String agent_name,
        String agent_system_prompt,
        AgentConfig config,
        List<KnowledgeBaseRef> knowledge_base,
        boolean record,
        String callback_url,
        List<String> callback_events,
        List<AvatarConfig> avatars,
        List<ToolConfig> tools
) {}