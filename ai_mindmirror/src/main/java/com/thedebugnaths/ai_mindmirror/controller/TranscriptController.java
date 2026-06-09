package com.thedebugnaths.ai_mindmirror.controller;

import com.thedebugnaths.ai_mindmirror.auth.SecurityUserDto;
import com.thedebugnaths.ai_mindmirror.dto.TranscriptResponseDto;
import com.thedebugnaths.ai_mindmirror.dto.trugen.TrugenConversationResponse;
import com.thedebugnaths.ai_mindmirror.service.TranscriptService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transcripts")
@RequiredArgsConstructor
public class TranscriptController {

    private final TranscriptService transcriptService;

    /**
     * Retrieves a single transcript by conversation ID.
     */
    @GetMapping("/{conversationId}")
    public ResponseEntity<TranscriptResponseDto> getTranscript(@PathVariable String conversationId) {
        TranscriptResponseDto response = transcriptService.getTranscriptDetails(conversationId);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves all completed transcripts for the currently authenticated user via JWT.
     */
    @GetMapping("/all")
    public ResponseEntity<List<TrugenConversationResponse>> getAllUserTranscripts(
            @AuthenticationPrincipal SecurityUserDto principal) {
        // Safely extracts the native database User object ID from your custom wrapper
        Long userId = principal.getUser().getId();
        return ResponseEntity.ok(transcriptService.getAllCompletedTranscriptsForUser(userId));
    }

    /**
     * Retrieves the 3 most recent transcripts for the authenticated user for LLM processing.
     */
    @GetMapping("/recent-three")
    public ResponseEntity<List<TrugenConversationResponse>> getRecentThreeForSummary(
            @AuthenticationPrincipal SecurityUserDto principal) {
        Long userId = principal.getUser().getId();
        return ResponseEntity.ok(transcriptService.getLastThreeCompletedTranscripts(userId));
    }
}