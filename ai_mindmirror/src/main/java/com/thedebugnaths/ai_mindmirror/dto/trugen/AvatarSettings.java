package com.thedebugnaths.ai_mindmirror.dto.trugen;

public record AvatarSettings(
        LlmConfig llm,
        SttConfig stt,
        TtsConfig tts
) {}
