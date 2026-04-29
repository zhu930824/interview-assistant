package interview.backend.common.config;

import interview.backend.infrastructure.speech.RealtimeSpeechWebSocketHandler;
import interview.backend.modules.voiceinterview.VoiceInterviewWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final VoiceInterviewWebSocketHandler voiceInterviewHandler;
    private final RealtimeSpeechWebSocketHandler speechHandler;

    public WebSocketConfig(VoiceInterviewWebSocketHandler voiceInterviewHandler,
                           RealtimeSpeechWebSocketHandler speechHandler) {
        this.voiceInterviewHandler = voiceInterviewHandler;
        this.speechHandler = speechHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(voiceInterviewHandler, "/ws/voice-interview").setAllowedOrigins("*");
        registry.addHandler(speechHandler, "/ws/realtime-speech").setAllowedOrigins("*");
    }
}
