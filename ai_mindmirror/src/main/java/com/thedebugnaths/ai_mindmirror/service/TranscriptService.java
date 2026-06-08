package com.thedebugnaths.ai_mindmirror.service;

import com.thedebugnaths.ai_mindmirror.dto.trugen.TrugenConversationResponse;
import com.thedebugnaths.ai_mindmirror.entity.Transcript;
import com.thedebugnaths.ai_mindmirror.exception.ResourceNotFoundException;
import com.thedebugnaths.ai_mindmirror.repository.TranscriptRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

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

    /**
     * Fetches the complete conversation transcript from Trugen and persists it natively as JSONB.
     */
    @Transactional
    public TrugenConversationResponse fetchAndSaveTranscript(String conversationId, Long userId) {
        try {
            TrugenConversationResponse payload = trugenClient.get()
                    .uri("/conversation/" + conversationId)
                    .retrieve()
                    .body(TrugenConversationResponse.class);

            if (payload == null || payload.id() == null) {
                throw new RuntimeException("Received an empty or unparseable payload from Trugen.");
            }

            Transcript transcriptEntity = new Transcript(payload.id(), userId, payload);
            transcriptRepository.save(transcriptEntity);

            System.out.println("Transcript saved: " + conversationId);

            return payload;

        } catch (Exception e) {
            System.err.println("Transcript sync failed for " + conversationId + ": " + e.getMessage());
            throw new RuntimeException("Transcript synchronization failed.", e);
        }
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
}