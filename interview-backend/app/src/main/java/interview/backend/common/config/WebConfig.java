package interview.backend.common.config;

import interview.backend.common.web.SimpleRateLimitInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final SimpleRateLimitInterceptor simpleRateLimitInterceptor;

    public WebConfig(SimpleRateLimitInterceptor simpleRateLimitInterceptor) {
        this.simpleRateLimitInterceptor = simpleRateLimitInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(simpleRateLimitInterceptor).addPathPatterns("/api/**");
    }
}
