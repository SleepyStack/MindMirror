package com.thedebugnaths.ai_mindmirror.controller;

import com.thedebugnaths.ai_mindmirror.dto.trugen.TrugenLifecycleRequest;
import com.thedebugnaths.ai_mindmirror.dto.trugen.TrugenWebhookRequest;
import com.thedebugnaths.ai_mindmirror.service.WebhookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/webhook/trugen")
@RequiredArgsConstructor
public class WebhookController {

    private final WebhookService webhookService;

    // Endpoint 1: Catches Agent lifecycle events (participant_left -> cleanup)
    @PostMapping
    public ResponseEntity<Object> handleLifecycleWebhook(
            @RequestParam("userId") Long userId,
            @RequestParam(value = "secret", required = false) String secret,
            @RequestBody TrugenLifecycleRequest payload) {
        boolean isAuthorized = webhookService.processLifecycleWebhook(secret, userId, payload);

        if (!isAuthorized) {
            System.out.println("Unauthorized lifecycle webhook blocked.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid signature"));
        }

        return ResponseEntity.ok(Map.of("status", "Lifecycle cleanup executed"));
    }

    // Endpoint 2: Catches native Tool Execution data payload
    @PostMapping("/tool")
    public ResponseEntity<Object> handleToolExecution(
            @RequestParam("userId") Long userId,
            @RequestParam(value = "secret", required = false) String querySecret,
            @RequestHeader(value = "X-Webhook-Secret", required = false) String headerSecret,
            @RequestBody TrugenWebhookRequest payload) { // <-- Keeps old TrugenWebhookRequest

        String secret = (headerSecret != null) ? headerSecret : querySecret;

        boolean isAuthorized = webhookService.processToolWebhook(secret, userId, payload);

        if (!isAuthorized) {
            System.out.println("Unauthorized tool webhook blocked.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid signature"));
        }

        return ResponseEntity.ok(Map.of("result", "Summary processed successfully."));
    }
}