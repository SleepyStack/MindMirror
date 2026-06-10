package com.thedebugnaths.ai_mindmirror.service;

import com.thedebugnaths.ai_mindmirror.dto.TranscriptResponseDto;
import com.thedebugnaths.ai_mindmirror.dto.trugen.TrugenConversationResponse;
import com.thedebugnaths.ai_mindmirror.entity.SyncStatus;
import com.thedebugnaths.ai_mindmirror.entity.Transcript;
import com.thedebugnaths.ai_mindmirror.exception.ResourceNotFoundException;
import com.thedebugnaths.ai_mindmirror.repository.TranscriptRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.util.List;

@Slf4j
@Service
public class TranscriptService {

    private final RestClient trugenClient;
    private final TranscriptRepository transcriptRepository;

    public TranscriptService(@Value("${trugen.api.key}") String trugenKey,
                             TranscriptRepository transcriptRepository) {
        this.transcriptRepository = transcriptRepository;

        this.trugenClient = RestClient.builder()
                .baseUrl("https://api.trugen.ai/v1/ext")
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("x-api-key", trugenKey)
                .build();
    }
    @Transactional
    public void registerPendingTranscript(String conversationId, Long userId) {
        // Save a placeholder record with null payload and PENDING status
        Transcript pendingTranscript = new Transcript(conversationId, userId, null, SyncStatus.PENDING);
        transcriptRepository.save(pendingTranscript);
        log.info("Registered pending transcript for background sync: {}", conversationId);
    }
    /**
     * Fetches a locally stored JSONB transcript from the PostgreSQL database by its unique conversation ID.
     */
    @Transactional(readOnly = true)
    public TrugenConversationResponse getTranscriptByConversationId(String conversationId) {
        Transcript transcript = transcriptRepository.findByConversationId(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Transcript not found for conversation: " + conversationId));

        return transcript.getPayload();
    }
    /**
     * Fetches all completed transcripts for a user to display on their dashboard.
     */
    @Transactional(readOnly = true)
    public List<TrugenConversationResponse> getAllCompletedTranscriptsForUser(Long userId) {
        return transcriptRepository.findAllByUserIdAndSyncStatusOrderByCreatedAtDesc(userId, SyncStatus.COMPLETED)
                .stream()
                .map(Transcript::getPayload)
                .toList();
    }

    /**
     * Fetches the 3 most recent, fully synced transcripts for the Weekly LLM Summary generation.
     */
    @Transactional(readOnly = true)
    public List<TrugenConversationResponse> getLastThreeCompletedTranscripts(Long userId) {
        return transcriptRepository.findTop3ByUserIdAndSyncStatusOrderByCreatedAtDesc(userId, SyncStatus.COMPLETED)
                .stream()
                .map(Transcript::getPayload) // Extracts the JSONB payload
                .toList();
    }

    @Transactional(readOnly = true)
    public TranscriptResponseDto getTranscriptDetails(String conversationId) {
        Transcript transcript = transcriptRepository.findByConversationId(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Transcript record not found for: " + conversationId));

        // Returns the status alongside the payload (which will be null if PENDING)
        return new TranscriptResponseDto(
                transcript.getConversationId(),
                transcript.getSyncStatus(),
                transcript.getPayload()
        );
    }
}