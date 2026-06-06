package com.thedebugnaths.ai_mindmirror.dto.trugen;

import java.util.List;

public record IdleTimeout(int timeout,
                   List<String> filler_phrases)
{}
