package com.thedebugnaths.ai_mindmirror.controller;

import com.thedebugnaths.ai_mindmirror.dto.trugen.TrugenConversationResponse;
import com.thedebugnaths.ai_mindmirror.service.TranscriptService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transcripts")
@RequiredArgsConstructor
public class TranscriptController {

    private final TranscriptService transcriptService;

    /**
     * Retrieves the raw or structural JSONB conversation history for a completed avatar session.
     */
    @GetMapping("/{conversationId}")
    public ResponseEntity<TrugenConversationResponse> getTranscript(@PathVariable String conversationId) {
        TrugenConversationResponse transcriptPayload = transcriptService.getTranscriptByConversationId(conversationId);
        return ResponseEntity.ok(transcriptPayload);
    }
}