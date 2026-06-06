package com.thedebugnaths.ai_mindmirror.dto.trugen;

public record AvatarConfig(
        String avatar_key_id,
        AvatarSettings config,
        String persona_name,
        String persona_prompt,
        String conversational_context,
        IdleTimeout idle_timeout,
        MessageGroup welcome_message,
        MessageGroup warning_exit_message,
        MessageGroup exit_message,
        MessageGroup exit_heads_up_message
) {}
