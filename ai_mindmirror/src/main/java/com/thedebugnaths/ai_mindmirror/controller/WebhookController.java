package com.thedebugnaths.ai_mindmirror.controller;

import com.thedebugnaths.ai_mindmirror.dto.TrugenWebhookRequest;
import com.thedebugnaths.ai_mindmirror.service.WebhookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/webhook")
@RequiredArgsConstructor
public class WebhookController {

    private final WebhookService webhookService;

    @PostMapping("/trugen")
    public ResponseEntity<String> handleTrugenWebhook(
            @RequestHeader(value = "X-Webhook-Secret", required = false) String incomingSecret,
            @RequestBody TrugenWebhookRequest payload) {

        // Pass the ID from AI's payload
        boolean isAuthorized = webhookService.processTrugenWebhook(incomingSecret, payload.userId(), payload);

        if (!isAuthorized) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid webhook signature");
        }

        return ResponseEntity.ok("Webhook processed successfully");
    }
}