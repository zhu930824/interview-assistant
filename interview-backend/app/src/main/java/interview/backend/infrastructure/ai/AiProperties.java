package interview.backend.infrastructure.ai;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.ai")
public record AiProperties(
        boolean enabled,
        String provider,
        String apiKey,
        String model,
        String baseUrl
) {
    public AiProperties() {
        this(false, "stub", "", "", "");
    }
}
