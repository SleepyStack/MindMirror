package com.thedebugnaths.ai_mindmirror.dto.trugen;

public record SttConfig(String model,
                 String provider,
                 double min_endpointing_delay,
                 double max_endpointing_delay)
{}
