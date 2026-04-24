package interview.backend.common.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import interview.backend.common.result.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class SimpleRateLimitInterceptor implements HandlerInterceptor {

    private static final long WINDOW_MILLIS = 60_000L;
    private static final int LIMIT = 120;

    private final Map<String, Counter> counters = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        var key = request.getRemoteAddr() + ":" + request.getRequestURI();
        var now = Instant.now().toEpochMilli();
        var counter = counters.compute(key, (ignored, existing) -> {
            if (existing == null || now - existing.windowStart() > WINDOW_MILLIS) {
                return new Counter(now, 1);
            }
            return new Counter(existing.windowStart(), existing.count() + 1);
        });
        if (counter.count() > LIMIT) {
            response.setStatus(429);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(objectMapper.writeValueAsString(ApiResponse.failure("请求过于频繁，请稍后重试")));
            return false;
        }
        return true;
    }

    private record Counter(long windowStart, int count) {
    }
}
