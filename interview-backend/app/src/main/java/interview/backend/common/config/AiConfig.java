package interview.backend.common.config;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import interview.backend.infrastructure.ai.AiService;
import interview.backend.infrastructure.ai.DashScopeAiService;
import interview.backend.infrastructure.ai.StubAiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * AI 服务配置类
 */
@Configuration
public class AiConfig {

    private static final Logger log = LoggerFactory.getLogger(AiConfig.class);

    @Bean
    @Primary
    @ConditionalOnProperty(name = "app.ai.provider", havingValue = "dashscope")
    public AiService dashScopeAiService(DashScopeChatModel chatModel) {
        log.info("Creating DashScopeAiService with chatModel: {}", chatModel.getClass().getName());
        return new DashScopeAiService(chatModel);
    }

    @Bean
    @ConditionalOnMissingBean(AiService.class)
    public AiService stubAiService() {
        log.info("Creating StubAiService (fallback)");
        return new StubAiService();
    }
}
