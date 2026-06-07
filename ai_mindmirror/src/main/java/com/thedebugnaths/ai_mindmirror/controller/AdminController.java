package com.thedebugnaths.ai_mindmirror.controller;

import com.thedebugnaths.ai_mindmirror.entity.Role;
import com.thedebugnaths.ai_mindmirror.entity.User;
import com.thedebugnaths.ai_mindmirror.repository.UserRepository;
import com.thedebugnaths.ai_mindmirror.repository.SessionHistoryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserRepository userRepository;
    private final SessionHistoryRepository sessionHistoryRepository;

    public AdminController(UserRepository userRepository, SessionHistoryRepository sessionHistoryRepository) {
        this.userRepository = userRepository;
        this.sessionHistoryRepository = sessionHistoryRepository;
    }

    @GetMapping("/metrics")
    public ResponseEntity<?> getSystemMetrics(@RequestAttribute("authenticatedUser") User caller) {
        if (caller.getRole() != Role.ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Access Denied: Admin privileges required."));
        }

        long totalUsers = userRepository.count();
        long totalSessions = sessionHistoryRepository.count();

        return ResponseEntity.ok(Map.of(
                "status", "HEALTHY",
                "totalUsers", totalUsers,
                "totalSessions", totalSessions
        ));
    }
}