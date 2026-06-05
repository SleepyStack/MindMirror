package com.thedebugnaths.ai_mindmirror.controller;

import com.thedebugnaths.ai_mindmirror.dto.TrugenWebhookRequest;
import com.thedebugnaths.ai_mindmirror.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/webhook")
@RequiredArgsConstructor
public class WebhookController {

    private final SessionService sessionService;

    @PostMapping("/trugen/{userId}")
    public ResponseEntity<String> handleTrugenWebhook(
            @PathVariable Long userId,
            @RequestBody TrugenWebhookRequest payload) {

        sessionService.saveSessionWebhook(userId, payload);
        return ResponseEntity.ok("Webhook processed successfully");
    }
}