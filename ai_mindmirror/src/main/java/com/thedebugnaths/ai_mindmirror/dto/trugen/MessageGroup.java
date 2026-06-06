package com.thedebugnaths.ai_mindmirror.dto.trugen;

import java.util.List;

public record MessageGroup(int wait_time,
                    List<String> messages)
{}
