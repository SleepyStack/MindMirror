package com.thedebugnaths.ai_mindmirror.service;

import com.thedebugnaths.ai_mindmirror.dto.trugen.TrugenConversationResponse;
import com.thedebugnaths.ai_mindmirror.entity.SyncStatus;
import com.thedebugnaths.ai_mindmirror.entity.Transcript;
import com.thedebugnaths.ai_mindmirror.repository.TranscriptRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.util.List;

@Slf4j
@Service
public class TranscriptCronWorker {

    private final TranscriptRepository transcriptRepository;
    private final RestClient trugenClient;

    public TranscriptCronWorker(@Value("${trugen.api.key}") String trugenKey,
                                TranscriptRepository transcriptRepository) {
        this.transcriptRepository = transcriptRepository;

        this.trugenClient = RestClient.builder()
                .baseUrl("https://api.trugen.ai/v1/ext")
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("x-api-key", trugenKey)
                .build();
    }

    // Runs every 60 seconds
    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void processPendingTranscripts() {
        List<Transcript> pendingTranscripts = transcriptRepository.findBySyncStatus(SyncStatus.PENDING);

        if (pendingTranscripts.isEmpty()) {
            return;
        }

        log.info("Cron Job executing: Found {} pending transcripts.", pendingTranscripts.size());

        for (Transcript transcript : pendingTranscripts) {
            try {
                TrugenConversationResponse payload = trugenClient.get()
                        .uri("/conversation/" + transcript.getConversationId())
                        .retrieve()
                        .body(TrugenConversationResponse.class);

                // Check if the remote payload actually contains the transcript data yet
                if (payload != null && payload.transcript() != null && !payload.transcript().isEmpty()) {
                    transcript.setPayload(payload);
                    transcript.setSyncStatus(SyncStatus.COMPLETED);
                    transcriptRepository.save(transcript);
                    log.info("Successfully synced transcript: {}", transcript.getConversationId());
                } else {
                    log.info("Transcript still processing on Trugen's end for: {}", transcript.getConversationId());
                }

            } catch (Exception e) {
                log.error("Polling failed for {}: {}", transcript.getConversationId(), e.getMessage());
            }
        }
    }
}