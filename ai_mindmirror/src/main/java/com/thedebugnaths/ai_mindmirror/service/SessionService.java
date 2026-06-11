package com.thedebugnaths.ai_mindmirror.service;

import com.thedebugnaths.ai_mindmirror.dto.SessionHistoryResponse;
import com.thedebugnaths.ai_mindmirror.dto.trugen.AgentProvisionResult;
import com.thedebugnaths.ai_mindmirror.dto.trugen.TrugenLifecycleRequest;
import com.thedebugnaths.ai_mindmirror.dto.trugen.TrugenWebhookRequest;
import com.thedebugnaths.ai_mindmirror.entity.SessionHistory;
import com.thedebugnaths.ai_mindmirror.entity.User;
import com.thedebugnaths.ai_mindmirror.exception.ResourceNotFoundException;
import com.thedebugnaths.ai_mindmirror.repository.SessionHistoryRepository;
import com.thedebugnaths.ai_mindmirror.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionService {

    private final SessionHistoryRepository sessionHistoryRepository;
    private final UserRepository userRepository;
    private final TrugenAgentService trugenAgentService;
    private final TranscriptService transcriptService;

    public String initializeTrugenSession(Long userId, String lang) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        AgentProvisionResult result = trugenAgentService.createEphemeralAgentForUser(userId, lang);

        SessionHistory activeSession = new SessionHistory();
        activeSession.setUser(user);
        activeSession.setAgentId(result.agentId());

        if (result.toolIds() != null && !result.toolIds().isEmpty()) {
            activeSession.setToolId(String.join(",", result.toolIds()));
        }

        activeSession.setStatus("ACTIVE");
        sessionHistoryRepository.save(activeSession);

        return "https://app.trugen.ai/embed/" + result.agentId();
    }

    public void terminateSessionResources(Long userId, TrugenLifecycleRequest payload) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return;

        SessionHistory activeSession = sessionHistoryRepository
                .findFirstByUserAndStatusOrderByIdDesc(user, "ACTIVE")
                .orElse(null);

        if (activeSession == null) {
            log.info("No active session found to terminate for user: {}", userId);
            return;
        }

        log.info("Executing cloud asset cleanup for User ID: {}", userId);

        // 1. Clean up ephemeral remote infrastructure resources
        if (activeSession.getAgentId() != null) {
            trugenAgentService.deleteAgent(activeSession.getAgentId());
        }

        if (activeSession.getToolId() != null && !activeSession.getToolId().isBlank()) {
            // Split the stored comma-separated string back into a List for the cleanup method
            List<String> toolsToClean = Arrays.asList(activeSession.getToolId().split(","));
            trugenAgentService.deleteTools(toolsToClean);
        }

        // 2. Map conversation id
        if (payload != null && payload.conversationId() != null) {
            String conversationId = payload.conversationId();
            activeSession.setConversationId(conversationId);
            transcriptService.registerPendingTranscript(conversationId, userId);
            log.info("Saved conversation ID: {}", conversationId);
        }

        activeSession.setStatus("COMPLETED");

        if (activeSession.getSummaryText() == null) {
            activeSession.setSummaryText("The user ended the session directly.");
            activeSession.setMainTopic("General Check-in");
        }

        sessionHistoryRepository.save(activeSession);
        log.info("Session closed successfully in database.");
    }

    public void saveSessionWebhook(Long userId, TrugenWebhookRequest payload) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return;

        SessionHistory activeSession = sessionHistoryRepository
                .findFirstByUserAndStatusOrderByIdDesc(user, "ACTIVE")
                .orElse(new SessionHistory());

        activeSession.setUser(user);
        activeSession.setSummaryText(payload.summary());
        activeSession.setMainTopic(payload.mainTopic());
        activeSession.setEmotionStart(payload.emotionStart());
        activeSession.setEmotionEnd(payload.emotionEnd());
        activeSession.setActionStep(payload.actionStep());

        sessionHistoryRepository.save(activeSession);
    }

    public List<SessionHistoryResponse> getUserDashboardHistory(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return sessionHistoryRepository.findAllByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(SessionHistoryResponse::fromEntity)
                .toList();
    }
}