package com.thedebugnaths.ai_mindmirror.service;

import com.thedebugnaths.ai_mindmirror.dto.trugen.*;
import com.thedebugnaths.ai_mindmirror.entity.SessionHistory;
import com.thedebugnaths.ai_mindmirror.entity.User;
import com.thedebugnaths.ai_mindmirror.exception.ResourceNotFoundException;
import com.thedebugnaths.ai_mindmirror.repository.SessionHistoryRepository;
import com.thedebugnaths.ai_mindmirror.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class TrugenAgentService {

    private final RestClient restClient;
    private final UserRepository userRepository;
    private final SessionHistoryRepository sessionHistoryRepository;

    @Value("${app.ngrok.url}")
    private String baseWebhookUrl;

    @Value("${webhook.secret.token}")
    private String webhookSecret;

    private static final String BASE_SYSTEM_PROMPT = """
        You are MindMirror.
        
        You are an emotionally aware AI companion.
        
        You are not a therapist, counselor, doctor, or crisis professional. You do not diagnose conditions or claim professional expertise.
        
        Your purpose is simple:
        Help people feel noticed, understood, supported, and less alone.
        
        You are not here to fix every problem.
        You are here to pay attention.
        
        ## Core Philosophy
        Most people do not need immediate solutions.
        They need someone who notices what they are experiencing.
        Before offering advice, make sure the user feels understood.
        Your goal is not to analyze people.
        Your goal is to connect with them.
        
        Always prioritize:
        1. Understanding
        2. Emotional connection
        3. Clarity
        4. Action
        In that order.
        
        ---
        
        ## Conversation Framework
        Whenever appropriate:
        1. Observe
        2. Reflect
        3. Ask
        4. Guide
        
        ### Observe
        Pay attention to:
        * emotional tone
        * confidence level
        * hesitation
        * frustration
        * sadness
        * loneliness
        * excitement
        * overwhelm
        * changes in energy
        * changes in engagement
        
        ### Reflect
        Briefly acknowledge what you notice. Keep reflections natural and concise.
        
        ### Ask
        Ask one thoughtful question that helps deepen understanding.
        
        ### Guide
        Offer one small next step. Never overwhelm the user with multiple exercises.
        """;

    private static final String SYSTEM_PROMPT_FOOTER = """
        ---
        
        ## Response Style
        Be: warm, calm, emotionally intelligent, concise, natural, supportive, curious.
        Avoid: sounding robotic, clinical, or like customer support.
        
        ## What Not To Do
        Do not diagnose conditions, shame the user, or overwhelm them with advice.
        
        ## Session Closing
        When the user indicates they are ready to end the session, say goodbye warmly. 
        If the user explicitly says 'end session', say goodbye immediately and wait silently for them to disconnect.
        """;

    public TrugenAgentService(@Value("${trugen.api.key}") String apiKey,
                              UserRepository userRepository,
                              SessionHistoryRepository sessionHistoryRepository) {
        this.userRepository = userRepository;
        this.sessionHistoryRepository = sessionHistoryRepository;
        this.restClient = RestClient.builder()
                .baseUrl("https://api.trugen.ai/v1/ext")
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("x-api-key", apiKey)
                .build();
    }

    public AgentProvisionResult createEphemeralAgentForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<SessionHistory> pastSessions = sessionHistoryRepository.findAllByUserOrderByCreatedAtDesc(user);

        StringBuilder promptBuilder = new StringBuilder(BASE_SYSTEM_PROMPT);

        if (pastSessions.isEmpty()) {
            promptBuilder.append("\n## Recent Session Context\nThis is your very first session with this user. Be welcoming.\n");
        } else {
            promptBuilder.append("\n## Recent Session Context\nYou have spoken with this user recently. Here are your private notes from previous sessions:\n\n");

            int limit = Math.min(pastSessions.size(), 3);
            for (int i = 0; i < limit; i++) {
                SessionHistory oldSession = pastSessions.get(i);

                promptBuilder.append("- Topic: ").append(oldSession.getMainTopic() != null ? oldSession.getMainTopic() : "General Check-in").append("\n");
                if (oldSession.getSummaryText() != null && !oldSession.getSummaryText().equals("The user ended the session directly.")) {
                    promptBuilder.append("  Summary: ").append(oldSession.getSummaryText()).append("\n");
                }
                if (oldSession.getActionStep() != null) {
                    promptBuilder.append("  Previous Advice Given: ").append(oldSession.getActionStep()).append("\n");
                }
                if (oldSession.getEmotionEnd() != null && !oldSession.getEmotionEnd().equals("Neutral")) {
                    promptBuilder.append("  User Left Feeling: ").append(oldSession.getEmotionEnd()).append("\n");
                }

                promptBuilder.append("\n");
            }
        }
        promptBuilder.append(SYSTEM_PROMPT_FOOTER);
        String compiledPrompt = promptBuilder.toString();

        // 1. Provision the Tool dynamically
        String toolId = provisionSessionSummaryTool(userId);

        String dynamicCallbackUrl = String.format("%s/api/webhook/trugen?userId=%d&secret=%s", baseWebhookUrl, userId, webhookSecret);

        TrugenAgentRequest requestBody = new TrugenAgentRequest(
                "Mind Mirror Session - User " + userId,
                compiledPrompt,
                new AgentConfig(240, new MemoryConfig(false, "")),
                List.of(
                        new KnowledgeBaseRef("594ee92c-8620-42c8-b949-525c93704885", "Mind Mirror Core KB"),
                        new KnowledgeBaseRef("77952c92-09c2-4c51-92f2-41e44be2c1ae", "Mind Mirror Behavioral Patterns")
                ),
                true,
                dynamicCallbackUrl,
                List.of("participant_left"),
                List.of(new AvatarConfig(
                        "665a1170",
                        new AvatarSettings(
                                new LlmConfig("gemini-3.1-pro-preview", "google"),
                                new SttConfig("flux-general-en", "deepgram", 0.3, 0.4),
                                new TtsConfig("eleven_turbo_v2_5", "elevenlabs", "FGY2WhTYpPnrIDTdsKH5")
                        ),
                        "Mind Mirror",
                        compiledPrompt,
                        "Providing empathetic emotional support.",
                        new IdleTimeout(30, List.of("Take your time. There's no rush.", "I'm right here with you.")),
                        new MessageGroup(2, List.of("Hi, I'm Mind Mirror, How are you doing today?")),
                        new MessageGroup(10, List.of("We have a few minutes left in our session.")),
                        new MessageGroup(300, List.of("Our session has wrapped up. Take care of yourself!")),
                        new MessageGroup(10, List.of("We are wrapping things up shortly."))
                )),
                List.of(new TrugenToolReference(toolId, "save_session_summary"))
        );

        // 2. Provision the Agent
        Map<String, Object> response = restClient.post()
                .uri("/agent")
                .body(requestBody)
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, Object>>() {});

        if (response != null && response.containsKey("id")) {
            String agentId = response.get("id").toString();
            return new AgentProvisionResult(agentId, toolId); // Return both for state tracking!
        }

        throw new RuntimeException("Failed to retrieve operational agent ID from Trugen cluster.");
    }

    private String provisionSessionSummaryTool(Long userId) {
        Map<String, Object> summaryProperties = Map.of(
                "summaryText", Map.of("type", "string", "description", "A 2-3 sentence summary."),
                "mainTopic", Map.of("type", "string", "description", "The primary topic of conversation."),
                "emotionStart", Map.of("type", "string", "description", "Starting emotion."),
                "emotionEnd", Map.of("type", "string", "description", "Ending emotion."),
                "actionStep", Map.of("type", "string", "description", "Concrete actionable step.")
        );

        Map<String, Object> summaryParameters = Map.of(
                "type", "object",
                "properties", summaryProperties,
                "required", List.of("summaryText", "mainTopic", "emotionStart", "emotionEnd", "actionStep")
        );

        ToolSchema schema = new ToolSchema(
                "function",
                "save_session_summary",
                "Called to save session data.",
                summaryParameters
        );

        String executionUrl = String.format("%s/api/webhook/trugen/tool?userId=%d&secret=%s", baseWebhookUrl, userId, webhookSecret);
        RequestConfig requestConfig = new RequestConfig("POST", executionUrl, Map.of("Content-Type", "application/json", "X-Webhook-Secret", webhookSecret));

        TrugenToolCreateRequest toolPayload = new TrugenToolCreateRequest(
                "tool.api",
                schema,
                requestConfig,
                Map.of("on_start", Map.of("message", "Processing..."), "on_success", Map.of("message", "Saved."))
        );

        Map<String, Object> toolResponse = restClient.post()
                .uri("/tool")
                .body(toolPayload)
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, Object>>() {});

        if (toolResponse != null && toolResponse.containsKey("id")) {
            return toolResponse.get("id").toString();
        }
        throw new RuntimeException("Failed to register dynamic tool definitions with Trugen cluster.");
    }

    // --- CLEANUP EXECUTION METHODS ---
    public void deleteAgent(String agentId) {
        try {
            restClient.delete().uri("/agent/" + agentId).retrieve().toBodilessEntity();
            System.out.println("Cleaned up Trugen Agent: " + agentId);
        } catch (Exception e) { System.err.println("Cleanup failed for agent " + agentId); }
    }

    public void deleteTool(String toolId) {
        try {
            restClient.delete().uri("/tool/" + toolId).retrieve().toBodilessEntity();
            System.out.println("Cleaned up Trugen Tool: " + toolId);
        } catch (Exception e) { System.err.println("Cleanup failed for tool " + toolId); }
    }
}