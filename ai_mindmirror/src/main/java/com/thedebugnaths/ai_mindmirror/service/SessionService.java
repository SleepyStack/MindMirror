package com.thedebugnaths.ai_mindmirror.service;

import com.thedebugnaths.ai_mindmirror.dto.trugen.AgentProvisionResult;
import com.thedebugnaths.ai_mindmirror.dto.trugen.TrugenLifecycleRequest;
import com.thedebugnaths.ai_mindmirror.dto.trugen.TrugenWebhookRequest;

import com.thedebugnaths.ai_mindmirror.entity.SessionHistory;
import com.thedebugnaths.ai_mindmirror.entity.User;
import com.thedebugnaths.ai_mindmirror.exception.ResourceNotFoundException;
import com.thedebugnaths.ai_mindmirror.repository.SessionHistoryRepository;
import com.thedebugnaths.ai_mindmirror.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final SessionHistoryRepository sessionHistoryRepository;
    private final UserRepository userRepository;
    private final TrugenAgentService trugenAgentService;

    public String initializeTrugenSession(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        AgentProvisionResult result = trugenAgentService.createEphemeralAgentForUser(userId);

        SessionHistory activeSession = new SessionHistory();
        activeSession.setUser(user);
        activeSession.setAgentId(result.agentId());
        activeSession.setToolId(result.toolId());
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
            System.out.println("No active session found to terminate for user: " + userId);
            return;
        }

        System.out.println("Executing cloud asset cleanup for User ID: " + userId);

        if (activeSession.getAgentId() != null) {
            trugenAgentService.deleteAgent(activeSession.getAgentId());
        }
        if (activeSession.getToolId() != null) {
            trugenAgentService.deleteTool(activeSession.getToolId());
        }

        if (payload != null && payload.conversationId() != null) {
            activeSession.setConversationId(payload.conversationId());
            System.out.println("Saved conversation ID: " + payload.conversationId());
        }

        activeSession.setStatus("COMPLETED");

        if (activeSession.getSummaryText() == null) {
            activeSession.setSummaryText("The user ended the session directly.");
            activeSession.setMainTopic("General Check-in");
        }

        sessionHistoryRepository.save(activeSession);
        System.out.println("Session closed successfully in database.");
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

    public List<SessionHistory> getUserDashboardHistory(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return sessionHistoryRepository.findAllByUserOrderByCreatedAtDesc(user);
    }
}