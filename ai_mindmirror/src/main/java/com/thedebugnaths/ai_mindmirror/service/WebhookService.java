package com.thedebugnaths.ai_mindmirror.service;

import com.thedebugnaths.ai_mindmirror.dto.BreatheExerciseWebhook;
import com.thedebugnaths.ai_mindmirror.dto.ChangeEmotionWebhook;
import com.thedebugnaths.ai_mindmirror.dto.trugen.TrugenLifecycleRequest;
import com.thedebugnaths.ai_mindmirror.dto.trugen.TrugenWebhookRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookService {

    private final SessionService sessionService;
    private final HardwareIntegrationService hardwareService; // <-- Injected Hardware Service

    @Value("${webhook.secret.token}")
    private String expectedSecret;

    // 1. Handles Endpoint 1: Disconnect / Lifecycle Events
    public boolean processLifecycleWebhook(String incomingSecret, Long userId, TrugenLifecycleRequest payload) {
        if (incomingSecret == null || !incomingSecret.equals(expectedSecret)) {
            return false; // Validation failed
        }

        // Check if the payload contains the participant_left event
        if (payload.event() != null && "participant_left".equals(payload.event().name())) {
            log.info("Participant disconnected. Triggering delete sequence for User: {}", userId);

            // Pass both the userId and the lifecycle payload to extract conversationId
            sessionService.terminateSessionResources(userId, payload);
        }

        return true;
    }

    // 2. Handles Endpoint 2: Mid-call AI Tool Summary Execution
    public boolean processToolWebhook(String incomingSecret, Long userId, TrugenWebhookRequest payload) {
        if (incomingSecret == null || !incomingSecret.equals(expectedSecret)) {
            return false; // Validation failed
        }

        // Route directly to the session service to store the conversation analytics
        sessionService.saveSessionWebhook(userId, payload);
        return true;
    }

    // 3. Handles Endpoint 3: Live Hardware Emotion Trigger
    public boolean processEmotionWebhook(String incomingSecret, Long userId, ChangeEmotionWebhook payload) {
        if (incomingSecret == null || !incomingSecret.equals(expectedSecret)) {
            return false; // Validation failed
        }

        if (payload != null && payload.emotion() != null) {
            hardwareService.triggerHardwareCommand(payload.emotion());
        } else {
            log.warn("Emotion trigger received, but the emotion string was missing from the payload!");
        }
        return true;
    }

    // 4. Handles Endpoint 4: Live Hardware Breathing Trigger
    public boolean processBreatheWebhook(String incomingSecret, Long userId, BreatheExerciseWebhook payload) {
        if (incomingSecret == null || !incomingSecret.equals(expectedSecret)) {
            return false; // Validation failed
        }

        hardwareService.triggerHardwareCommand("breath");
        return true;
    }
}