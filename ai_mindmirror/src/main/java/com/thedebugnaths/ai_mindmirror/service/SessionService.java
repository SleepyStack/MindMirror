package com.thedebugnaths.ai_mindmirror.service;

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
        String agentId = trugenAgentService.createEphemeralAgentForUser(userId);
        return "https://app.trugen.ai/embed/" + agentId;
    }

    public void saveSessionWebhook(Long userId, TrugenWebhookRequest payload) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        SessionHistory history = new SessionHistory();
        history.setUser(user);

        history.setSummaryText(payload.summary());
        history.setMainTopic(payload.mainTopic());
        history.setEmotionStart(payload.emotionStart());
        history.setEmotionEnd(payload.emotionEnd());
        history.setActionStep(payload.actionStep());

        sessionHistoryRepository.save(history);
    }

    public List<SessionHistory> getUserDashboardHistory(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return sessionHistoryRepository.findAllByUserOrderByCreatedAtDesc(user);
    }
}