package com.thedebugnaths.ai_mindmirror.controller;

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

    // 1. Catches standard Agent lifecycle event
    @PostMapping
    public ResponseEntity<Object> handleLifecycleWebhook(
            @RequestParam("userId") Long userId,
            @RequestParam(value = "secret", required = false) String secret,
            @RequestBody TrugenWebhookRequest payload) {

        boolean isAuthorized = webhookService.processTrugenWebhook(secret, userId, payload);

        if (!isAuthorized) {
            System.out.println("Unauthorized lifecycle webhook attempt blocked!");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid webhook signature"));
        }

        return ResponseEntity.ok(Map.of("status", "Lifecycle event processed"));
    }

    // 2. Catches native Tool Execution data payload
    @PostMapping("/tool")
    public ResponseEntity<Object> handleToolExecution(
            @RequestParam("userId") Long userId,
            @RequestParam(value = "secret", required = false) String querySecret,
            @RequestHeader(value = "X-Webhook-Secret", required = false) String headerSecret,
            @RequestBody TrugenWebhookRequest payload) {

        // Fallback check: Use header if present, otherwise fall back to query param
        String secret = (headerSecret != null) ? headerSecret : querySecret;

        boolean isAuthorized = webhookService.processTrugenWebhook(secret, userId, payload);

        if (!isAuthorized) {
            System.out.println("Unauthorized tool webhook attempt blocked!");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid webhook signature"));
        }

        // Return a JSON success message that Trugen can feed back to the LLM
        return ResponseEntity.ok(Map.of("result", "Summary successfully saved to database."));
    }
}