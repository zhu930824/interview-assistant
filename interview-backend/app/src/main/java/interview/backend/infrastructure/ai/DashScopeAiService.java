package interview.backend.infrastructure.ai;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DashScope AI 服务实现
 */
public class DashScopeAiService implements AiService {

    private static final Logger log = LoggerFactory.getLogger(DashScopeAiService.class);

    private final DashScopeChatModel chatModel;

    public DashScopeAiService(DashScopeChatModel chatModel) {
        this.chatModel = chatModel;
        log.info("DashScopeAiService initialized successfully");
    }

    @Override
    public String chat(String systemPrompt, String userContent) {
        log.info("DashScopeAiService.chat() called - systemPrompt: {}, contentLength: {}",
            truncate(systemPrompt, 50), userContent == null ? 0 : userContent.length());

        if (userContent == null || userContent.isBlank()) {
            log.warn("User content is empty");
            return "{\"error\": \"内容为空\"}";
        }

        try {
            Prompt prompt = new Prompt(userContent);

            log.info("Calling DashScope API...");
            ChatResponse response = chatModel.call(prompt);
            String result = response.getResult().getOutput().getText();

            log.info("DashScope API response length: {}", result == null ? 0 : result.length());
            log.debug("DashScope API response: {}", truncate(result, 500));
            return result;
        } catch (Exception e) {
            log.error("DashScope AI call failed: {}", e.getMessage(), e);
            throw new RuntimeException("AI 服务调用失败: " + e.getMessage(), e);
        }
    }

    private String truncate(String str, int maxLength) {
        if (str == null) return "null";
        if (str.length() <= maxLength) return str;
        return str.substring(0, maxLength) + "...";
    }
}
