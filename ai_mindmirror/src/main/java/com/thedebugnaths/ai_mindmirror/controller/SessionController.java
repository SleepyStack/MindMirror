package com.thedebugnaths.ai_mindmirror.controller;

import com.thedebugnaths.ai_mindmirror.dto.SessionHistoryResponse;
import com.thedebugnaths.ai_mindmirror.entity.User;
import com.thedebugnaths.ai_mindmirror.exception.ResourceNotFoundException;
import com.thedebugnaths.ai_mindmirror.repository.UserRepository;
import com.thedebugnaths.ai_mindmirror.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/session")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;
    private final UserRepository userRepository;

    @PostMapping("/start/{lang}")
    public ResponseEntity<Map<String, String>> startSession(
            @PathVariable String lang,
            Authentication authentication) {

        String userEmail = authentication.getName();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Pass the language preference down to your session engine layer
        String trugenUrl = sessionService.initializeTrugenSession(user.getId(), lang);

        return ResponseEntity.ok(Map.of("url", trugenUrl));
    }

    @GetMapping("/history")
    public ResponseEntity<List<SessionHistoryResponse>> getHistory(Authentication authentication) {
        String userEmail = authentication.getName();
        return ResponseEntity.ok(sessionService.getUserDashboardHistory(userEmail));
    }
}