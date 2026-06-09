package com.thedebugnaths.ai_mindmirror.dto;

import com.thedebugnaths.ai_mindmirror.dto.trugen.TrugenConversationResponse;
import com.thedebugnaths.ai_mindmirror.entity.SyncStatus;

public record TranscriptResponseDto(
        String conversationId,
        SyncStatus syncStatus,
        TrugenConversationResponse payload
) {}