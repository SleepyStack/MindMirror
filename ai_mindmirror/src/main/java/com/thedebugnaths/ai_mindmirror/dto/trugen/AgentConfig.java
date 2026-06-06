package com.thedebugnaths.ai_mindmirror.dto.trugen;

public record AgentConfig(
        int timeout,
        MemoryConfig memory
) {}
