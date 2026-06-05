package com.thedebugnaths.ai_mindmirror.service;

import com.thedebugnaths.ai_mindmirror.entity.SessionHistory;
import com.thedebugnaths.ai_mindmirror.entity.User;
import com.thedebugnaths.ai_mindmirror.repository.SessionHistoryRepository;
import com.thedebugnaths.ai_mindmirror.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final SessionHistoryRepository sessionHistoryRepository;
    private final UserRepository userRepository;

    // BASE PROMPT: The core identity of your AI agent
    private static final String BASE_SYSTEM_PROMPT =
            "You are MindMirror, an empathetic emotional support AI agent. Your goal is to listen, " +
                    "validate feelings, and offer a safe space for the user. ";

    public String initializeTrugenSession(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // 1. Fetch the last 3 sessions for this specific user
        List<SessionHistory> pastSessions = sessionHistoryRepository.findTop3ByUserOrderByCreatedAtDesc(user);

        // 2. Build the dynamic system prompt
        String finalSystemPrompt;

        if (pastSessions.isEmpty()) {
            // Cold-start fallback
            finalSystemPrompt = BASE_SYSTEM_PROMPT +
                    "This is your very first session with this user. Be welcoming, introduce yourself, " +
                    "and gently ask how they are feeling today to begin building rapport.";
        } else {
            // Context injection loop
            StringBuilder contextBuilder = new StringBuilder(BASE_SYSTEM_PROMPT);
            contextBuilder.append("Here is a brief context on the user's recent emotional history for continuity:\n");

            for (int i = pastSessions.size() - 1; i >= 0; i--) { // Read chronological order
                SessionHistory session = pastSessions.get(i);
                contextBuilder.append(String.format("- Session: Main topic was '%s'. Started with %s, ended with %s. Summary: %s\n",
                        session.getMainTopic(), session.getEmotionStart(), session.getEmotionEnd(), session.getSummaryText()));
            }
            contextBuilder.append("Use this history subtly to show continuity. Do not explicitly say 'According to your records', " +
                    "but act like an ongoing counselor who remembers their past struggles.");
            finalSystemPrompt = contextBuilder.toString();
        }

        // 3. TODO: Make HTTP Call to TRUGEN REST API passing 'finalSystemPrompt'
        // For right now, let's log it to ensure it compiles perfectly
        System.out.println("--- COMPILED TRUGEN PROMPT ---");
        System.out.println(finalSystemPrompt);

        // Mocking the returned URL that you will eventually parse from TRUGEN's JSON response
        return "https://api.trugen.ai/v1/stream/mock-session-xyz-" + userId;
    }
}