package interview.backend.infrastructure.redis;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Service;

@Service
public class InMemoryRedisStreamService implements RedisStreamService {

    private final List<StreamMessage> messages = new CopyOnWriteArrayList<>();

    @Override
    public void publish(String stream, String key, String payload) {
        messages.add(new StreamMessage(stream, key, payload, LocalDateTime.now()));
    }

    public List<StreamMessage> getMessages() {
        return messages;
    }
}
