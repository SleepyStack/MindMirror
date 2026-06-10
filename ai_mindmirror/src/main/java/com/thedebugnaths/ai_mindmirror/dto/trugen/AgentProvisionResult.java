package com.thedebugnaths.ai_mindmirror.dto.trugen;

import java.util.List;

public record AgentProvisionResult(String agentId,
                                   List<String> toolIds)
{}
