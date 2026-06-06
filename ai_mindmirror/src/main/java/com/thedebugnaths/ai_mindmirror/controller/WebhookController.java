package com.thedebugnaths.ai_mindmirror.controller;

import com.thedebugnaths.ai_mindmirror.dto.trugen.TrugenWebhookRequest;
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
            @RequestParam("userId") Long userId,
            @RequestParam("secret") String secret,
            @RequestBody TrugenWebhookRequest payload) {
        boolean isAuthorized = webhookService.processTrugenWebhook(secret, userId, payload);

        if (!isAuthorized) {
            System.out.println("Unauthorized webhook attempt blocked!");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid webhook signature");
        }

        return ResponseEntity.ok("Webhook processed successfully");
    }
}