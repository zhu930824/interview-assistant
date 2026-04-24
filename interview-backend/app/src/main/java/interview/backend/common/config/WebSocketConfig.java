package interview.backend.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final interview.backend.modules.voiceinterview.VoiceInterviewWebSocketHandler handler;

    public WebSocketConfig(interview.backend.modules.voiceinterview.VoiceInterviewWebSocketHandler handler) {
        this.handler = handler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(handler, "/ws/voice-interview").setAllowedOrigins("*");
    }
}
