package interview.backend.infrastructure.redis;

public interface RedisStreamService {

    void publish(String stream, String key, String payload);
}
