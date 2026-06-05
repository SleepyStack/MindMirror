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

        String injectionInstruction = String.format(
                "\n\n[CRITICAL SYSTEM INSTRUCTION: You are currently speaking to User ID: %d. " +
                        "When this session ends and you trigger your data-saving tool, you MUST include 'userId': %d in the JSON payload.]",
                userId, userId);

        String finalPrompt = dynamicSystemPrompt + injectionInstruction;

        try {
            // Attempt to dynamically update your existing agent's prompt with the memory context
            restClient.put()
                    .uri("https://api.trugen.ai/v1/ext/agent/" + defaultAgentId)
                    .header("x-api-key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("agent_system_prompt", finalPrompt))
                    .retrieve()
                    .toBodilessEntity();

            System.out.println("✅ Successfully injected User " + userId + " context into Agent!");

        } catch (Exception e) {
            // If the API fails, don't crash the server! Just print a warning.
            System.out.println("⚠️ Warning: Could not update Agent prompt via API. Check TruGen keys/IDs.");
        }

        // Return the actual iframe embed URL required by TruGen's documentation
        return "https://app.trugen.ai/embed/" + defaultAgentId;
    }
}