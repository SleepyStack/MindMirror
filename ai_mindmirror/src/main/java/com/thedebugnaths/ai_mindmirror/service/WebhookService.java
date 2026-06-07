package com.thedebugnaths.ai_mindmirror.service;

import com.thedebugnaths.ai_mindmirror.dto.trugen.TrugenLifecycleRequest;
import com.thedebugnaths.ai_mindmirror.dto.trugen.TrugenWebhookRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WebhookService {

    private final SessionService sessionService;

    @Value("${webhook.secret.token}")
    private String expectedSecret;

    // 1. Handles Endpoint 1: Disconnect / Lifecycle Events
    public boolean processLifecycleWebhook(String incomingSecret, Long userId, TrugenLifecycleRequest payload) {
        if (incomingSecret == null || !incomingSecret.equals(expectedSecret)) {
            return false; // Validation failed
        }

        // Check if the payload contains the participant_left event
        if (payload.event() != null && "participant_left".equals(payload.event().name())) {
            System.out.println("Participant disconnected. Triggering delete sequence for User: " + userId);

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
}