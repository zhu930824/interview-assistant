package interview.backend.modules.voiceinterview;

import java.io.IOException;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class VoiceInterviewWebSocketHandler extends TextWebSocketHandler {

    private final VoiceInterviewService voiceInterviewService;

    public VoiceInterviewWebSocketHandler(VoiceInterviewService voiceInterviewService) {
        this.voiceInterviewService = voiceInterviewService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws IOException {
        session.sendMessage(new TextMessage("""
                {"type":"connected","message":"voice interview websocket connected"}
                """));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        var sessionIdQuery = session.getUri() != null ? session.getUri().getQuery() : null;
        var effectiveSessionId = extractSessionId(sessionIdQuery);
        if (effectiveSessionId == null) {
            session.sendMessage(new TextMessage("""
                    {"type":"error","message":"missing sessionId query parameter"}
                    """));
            return;
        }

        var payload = message.getPayload().replace("\"", "'");
        var result = voiceInterviewService.buildRealtimeReply(effectiveSessionId, payload);
        var response = """
                {"type":"subtitle","content":"%s","intermediate":false,"reply":"%s","question":"%s"}
                """.formatted(
                escape(result.get("subtitle")),
                escape(result.get("reply")),
                escape(result.get("question"))
        );
        session.sendMessage(new TextMessage(response));
    }

    private String extractSessionId(String query) {
        if (query == null || query.isBlank()) {
            return null;
        }
        for (var token : query.split("&")) {
            var pair = token.split("=", 2);
            if (pair.length == 2 && "sessionId".equals(pair[0])) {
                return pair[1];
            }
        }
        return null;
    }

    private String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "'");
    }
}
