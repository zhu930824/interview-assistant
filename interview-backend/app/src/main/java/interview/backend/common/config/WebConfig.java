package interview.backend.common.config;

import interview.backend.common.security.JwtAuthenticationInterceptor;
import interview.backend.common.web.SimpleRateLimitInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final SimpleRateLimitInterceptor simpleRateLimitInterceptor;
    private final JwtAuthenticationInterceptor jwtAuthenticationInterceptor;

    public WebConfig(SimpleRateLimitInterceptor simpleRateLimitInterceptor,
                     JwtAuthenticationInterceptor jwtAuthenticationInterceptor) {
        this.simpleRateLimitInterceptor = simpleRateLimitInterceptor;
        this.jwtAuthenticationInterceptor = jwtAuthenticationInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(simpleRateLimitInterceptor).addPathPatterns("/api/**");

        registry.addInterceptor(jwtAuthenticationInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/auth/login",
                        "/api/auth/register"
                );
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:5173", "http://localhost:5174", "http://localhost:5175")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
