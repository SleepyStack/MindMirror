package com.thedebugnaths.ai_mindmirror.service;

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

    public boolean processTrugenWebhook(String incomingSecret, Long userId, TrugenWebhookRequest payload) {
        if (incomingSecret == null || !incomingSecret.equals(expectedSecret)) {
            return false;
        }

        // Prevent DB failures if a lifecycle event (like participant_left) maps fields to null
        if (payload.summary() == null && payload.mainTopic() == null) {
            System.out.println("Lifecycle event captured for user: " + userId + ". Skipping database persistence.");
            return true;
        }

        sessionService.saveSessionWebhook(userId, payload);
        return true;
    }
}