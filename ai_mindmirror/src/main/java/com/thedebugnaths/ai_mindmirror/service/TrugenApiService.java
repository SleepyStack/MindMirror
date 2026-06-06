package com.thedebugnaths.ai_mindmirror.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
public class TrugenApiService {

    private final RestClient restClient = RestClient.create();

    @Value("${trugen.api.key:dummy_key}")
    private String apiKey;

    @Value("${trugen.agent.id:dummy_id}")
    private String defaultAgentId;

    public String createConversationSession(Long userId, String dynamicSystemPrompt) {

        try {
            // Attempt to dynamically update your existing agent's prompt with the memory context
            restClient.put()
                    .uri("https://api.trugen.ai/v1/ext/agent/" + defaultAgentId)
                    .header("x-api-key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("agent_system_prompt", dynamicSystemPrompt))
                    .retrieve()
                    .toBodilessEntity();

        } catch (Exception e) {
            // If the API fails, don't crash.
            System.out.println("API Update Skipped");
        }

        // Return the actual iframe embed URL required by TruGen's documentation
        return "https://app.trugen.ai/embed/" + defaultAgentId + "?userId=" + userId;
    }
}