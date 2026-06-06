package com.thedebugnaths.ai_mindmirror.dto.trugen;

import java.util.Map;

public record FunctionConfig(
        String name,
        String description,
        Map<String, Object> parameters
) {}