package interview.backend.infrastructure.redis;

import java.time.LocalDateTime;

public record StreamMessage(
        String stream,
        String key,
        String payload,
        LocalDateTime createdAt
) {
}
