package interview.backend.infrastructure.ai;

public interface AiService {
    /**
     * Single-turn chat, returns AI text response.
     */
    String chat(String systemPrompt, String userMessage);
}
