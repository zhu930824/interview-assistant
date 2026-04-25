package interview.backend.infrastructure.ai;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@ConditionalOnProperty(name = "app.ai.enabled", havingValue = "false", matchIfMissing = true)
public class StubAiService implements AiService {

    @Override
    public String chat(String systemPrompt, String userMessage) {
        // Resume analysis stub
        if (systemPrompt != null && systemPrompt.toLowerCase().contains("resume")) {
            return analyzeResumeStub(userMessage);
        }
        // Interview evaluation stub
        if (systemPrompt != null && systemPrompt.toLowerCase().contains("interview")) {
            return evaluateInterviewStub(userMessage);
        }
        // Schedule extraction stub
        if (systemPrompt != null && systemPrompt.toLowerCase().contains("schedule")) {
            return extractScheduleStub(userMessage);
        }
        // Knowledge QA stub
        if (systemPrompt != null && systemPrompt.toLowerCase().contains("knowledge")) {
            return answerKnowledgeStub(userMessage);
        }
        // Voice interview stub
        if (systemPrompt != null && systemPrompt.toLowerCase().contains("voice")) {
            return voiceReplyStub(userMessage);
        }
        return "AI response placeholder for: " + truncate(userMessage, 100);
    }

    @Override
    public Flux<String> chatStream(String systemPrompt, String userMessage) {
        return Flux.just("Streaming not implemented in stub mode. Response: ", chat(systemPrompt, userMessage));
    }

    private String analyzeResumeStub(String resumeText) {
        var name = detectName(resumeText);
        var position = detectPosition(resumeText);
        return String.format("""
                {"candidateName":"%s","targetPosition":"%s","keywords":["java","spring","mysql"],"strengths":["Relevant technical stack coverage"],"risks":["Project impact not quantified"],"summary":"Candidate %s appears aligned with %s."}
                """, name, position, name, position);
    }

    private String evaluateInterviewStub(String transcript) {
        var score = Math.min(85, 50 + transcript.length() / 10);
        return String.format("""
                {"score":%d,"strengths":["Response structure is complete"],"risks":["Need deeper project details"],"feedback":"Overall performance is satisfactory with areas for improvement."}
                """, score);
    }

    private String extractScheduleStub(String content) {
        return String.format("""
                {"company":"Unknown Company","position":"Backend Engineer","meetingLink":"https://meeting.example.com","startTime":"%s"}
                """, java.time.LocalDateTime.now().plusDays(1).toString());
    }

    private String answerKnowledgeStub(String question) {
        return "Based on the knowledge base: " + truncate(question, 50) + " - This is a placeholder AI response.";
    }

    private String voiceReplyStub(String transcript) {
        if (transcript.toLowerCase().contains("redis") || transcript.toLowerCase().contains("cache")) {
            return "Good. Why was Redis a better choice than local cache or database reads in that scenario?";
        }
        if (transcript.toLowerCase().contains("project")) {
            return "Please quantify the project impact with latency, throughput, stability, or delivery metrics.";
        }
        return "Please drill deeper into architecture, trade-offs, and measurable business impact.";
    }

    private String detectName(String text) {
        var lines = text.lines().map(String::trim).filter(l -> !l.isBlank()).limit(8).toList();
        for (var line : lines) {
            if (line.length() >= 2 && line.length() <= 40 && line.chars().noneMatch(Character::isDigit)) {
                return line;
            }
        }
        return "Unknown Candidate";
    }

    private String detectPosition(String text) {
        var lower = text.toLowerCase();
        if (lower.contains("java")) return "Java Backend Engineer";
        if (lower.contains("frontend") || lower.contains("vue") || lower.contains("react")) return "Frontend Engineer";
        if (lower.contains("python")) return "Python Engineer";
        return "Backend Engineer";
    }

    private String truncate(String text, int max) {
        if (text == null) return "";
        return text.length() <= max ? text : text.substring(0, max) + "...";
    }
}
