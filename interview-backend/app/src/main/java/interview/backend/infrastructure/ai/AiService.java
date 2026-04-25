package interview.backend.infrastructure.ai;

import reactor.core.publisher.Flux;

public interface AiService {
    /**
     * Single-turn chat, returns AI text response.
     */
    String chat(String systemPrompt, String userMessage);

    /**
     * Streaming chat, returns SSE event stream.
     */
    Flux<String> chatStream(String systemPrompt, String userMessage);
}
