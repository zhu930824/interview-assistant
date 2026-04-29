package interview.backend.infrastructure.speech;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RealtimeSpeechWebSocketHandler implements WebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(RealtimeSpeechWebSocketHandler.class);
    private static final String DASHSCOPE_WS_URL = "wss://dashscope.aliyuncs.com/api-ws/v1/inference/";

    private final String apiKey;
    private final String model;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 存储每个前端会话对应的阿里百炼 WebSocket 客户端
    private final ConcurrentHashMap<String, WebSocketClient> dashscopeClients = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, StringBuilder> transcriptionResults = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, WebSocketSession> frontendSessions = new ConcurrentHashMap<>();

    public RealtimeSpeechWebSocketHandler(
            @Value("${spring.ai.dashscope.api-key:}") String apiKey,
            @Value("${app.speech.alibaba.model:paraformer-realtime-v2}") String model) {
        this.apiKey = apiKey;
        this.model = model;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("前端 WebSocket 连接建立: {}", session.getId());

        if (apiKey == null || apiKey.isBlank()) {
            session.sendMessage(new TextMessage("{\"error\": \"API key 未配置\"}"));
            session.close();
            return;
        }

        frontendSessions.put(session.getId(), session);
        transcriptionResults.put(session.getId(), new StringBuilder());

        // 连接阿里百炼 WebSocket
        connectToDashscope(session.getId(), session);
    }

    private void connectToDashscope(String sessionId, WebSocketSession frontendSession) {
        try {
            URI uri = new URI(DASHSCOPE_WS_URL);

            WebSocketClient client = new WebSocketClient(uri) {
                @Override
                public void onOpen(ServerHandshake handshake) {
                    log.info("阿里百炼 WebSocket 连接成功: {}", sessionId);
                    // 发送启动消息
                    try {
                        ObjectNode startMessage = objectMapper.createObjectNode();
                        ObjectNode header = objectMapper.createObjectNode();
                        header.put("action", "run-task");
                        header.put("streaming", "duplex");
                        header.put("task_id", sessionId);
                        startMessage.set("header", header);

                        ObjectNode payload = objectMapper.createObjectNode();
                        payload.put("task_group", "audio");
                        payload.put("task", "asr");
                        payload.put("function", "recognition");
                        payload.put("model", model);

                        // input 字段为空对象
                        payload.set("input", objectMapper.createObjectNode());

                        // parameters 字段包含 format、sample_rate 等
                        ObjectNode parameters = objectMapper.createObjectNode();
                        parameters.put("format", "pcm");
                        parameters.put("sample_rate", 16000);
                        parameters.set("language_hints", objectMapper.createArrayNode().add("zh"));
                        payload.set("parameters", parameters);

                        startMessage.set("payload", payload);

                        this.send(startMessage.toString());
                        log.info("已发送启动消息: {}", startMessage.toString());
                    } catch (Exception e) {
                        log.error("发送启动消息失败", e);
                    }
                }

                @Override
                public void onMessage(String message) {
                    try {
                        log.info("收到阿里百炼消息: {}", message);
                        JsonNode root = objectMapper.readTree(message);
                        JsonNode header = root.path("header");
                        JsonNode payload = root.path("payload");

                        String event = header.path("event").asText();

                        switch (event) {
                            case "task-started":
                                log.info("转录任务已启动: {}", sessionId);
                                frontendSession.sendMessage(new TextMessage("{\"status\": \"started\"}"));
                                break;

                            case "result-generated":
                                // 处理识别结果 - payload.output.sentence
                                JsonNode sentence = payload.path("output").path("sentence");
                                String text = sentence.path("text").asText();
                                boolean isFinal = sentence.path("sentence_end").asBoolean(false);

                                if (text != null && !text.isEmpty()) {
                                    if (isFinal) {
                                        transcriptionResults.get(sessionId).append(text);
                                    }
                                    String response = objectMapper.writeValueAsString(
                                            objectMapper.createObjectNode()
                                                    .put("text", text)
                                                    .put("isFinal", isFinal)
                                    );
                                    frontendSession.sendMessage(new TextMessage(response));
                                }
                                break;

                            case "task-finished":
                                log.info("转录任务完成: {}", sessionId);
                                String fullText = transcriptionResults.get(sessionId).toString();
                                String completeResponse = objectMapper.writeValueAsString(
                                        objectMapper.createObjectNode()
                                                .put("status", "completed")
                                                .put("fullText", fullText)
                                );
                                frontendSession.sendMessage(new TextMessage(completeResponse));
                                break;

                            case "task-failed":
                                String code = header.path("error_code").asText();
                                String errorMessage = header.path("error_message").asText();
                                log.error("转录任务失败: code={}, message={}", code, errorMessage);
                                frontendSession.sendMessage(new TextMessage("{\"error\": \"" + code + ": " + errorMessage + "\"}"));
                                break;

                            default:
                                log.info("未知事件: {}", event);
                        }
                    } catch (Exception e) {
                        log.error("处理阿里百炼消息失败", e);
                    }
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    log.info("阿里百炼 WebSocket 关闭: {} - {}", code, reason);
                    dashscopeClients.remove(sessionId);
                }

                @Override
                public void onError(Exception ex) {
                    log.error("阿里百炼 WebSocket 错误", ex);
                    try {
                        frontendSession.sendMessage(new TextMessage("{\"error\": \"" + ex.getMessage() + "\"}"));
                    } catch (Exception e) {
                        log.error("发送错误消息失败", e);
                    }
                }
            };

            // 设置请求头
            client.addHeader("Authorization", "Bearer " + apiKey);
            client.connect();

            dashscopeClients.put(sessionId, client);

        } catch (Exception e) {
            log.error("连接阿里百炼失败", e);
            try {
                frontendSession.sendMessage(new TextMessage("{\"error\": \"连接语音识别服务失败\"}"));
            } catch (Exception ex) {
                log.error("发送错误消息失败", ex);
            }
        }
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        WebSocketClient client = dashscopeClients.get(session.getId());
        if (client == null || !client.isOpen()) {
            log.warn("阿里百炼连接未建立或已关闭");
            return;
        }

        if (message instanceof BinaryMessage) {
            // 发送音频数据到阿里百炼 - 直接发送二进制数据
            ByteBuffer buffer = ((BinaryMessage) message).getPayload();
            byte[] audioData = new byte[buffer.remaining()];
            buffer.get(audioData);

            // 直接发送二进制音频数据
            client.send(audioData);

        } else if (message instanceof TextMessage) {
            String payload = message.getPayload().toString();
            if ("stop".equals(payload)) {
                // 发送停止消息
                ObjectNode stopMessage = objectMapper.createObjectNode();
                ObjectNode stopHeader = objectMapper.createObjectNode();
                stopHeader.put("action", "finish-task");
                stopHeader.put("task_id", session.getId());
                stopHeader.put("streaming", "duplex");
                stopMessage.set("header", stopHeader);
                stopMessage.set("payload", objectMapper.createObjectNode()
                        .set("input", objectMapper.createObjectNode()));
                client.send(stopMessage.toString());
            }
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("前端 WebSocket 传输错误: {}", exception.getMessage());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("前端 WebSocket 连接关闭: {}", session.getId());

        // 关闭阿里百炼连接
        WebSocketClient client = dashscopeClients.remove(session.getId());
        if (client != null) {
            client.close();
        }

        transcriptionResults.remove(session.getId());
        frontendSessions.remove(session.getId());
    }

    @Override
    public boolean supportsPartialMessages() {
        return true;
    }
}