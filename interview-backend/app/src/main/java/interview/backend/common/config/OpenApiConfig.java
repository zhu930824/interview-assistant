package interview.backend.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI interviewOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Interview Assistant API")
                .version("1.0.0")
                .description("面试与简历管理系统接口文档"));
    }
}
