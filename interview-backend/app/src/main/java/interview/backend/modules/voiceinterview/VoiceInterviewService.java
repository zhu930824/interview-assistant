package interview.backend.modules.voiceinterview;

import interview.backend.common.exception.BizException;
import interview.backend.infrastructure.export.PdfExportService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class VoiceInterviewService {

    private final Map<String, VoiceInterviewSession> sessions = new ConcurrentHashMap<>();
    private final PdfExportService pdfExportService;

    public VoiceInterviewService(PdfExportService pdfExportService) {
        this.pdfExportService = pdfExportService;
    }

    public VoiceInterviewSession create() {
        var now = LocalDateTime.now();
        var session = new VoiceInterviewSession(
                UUID.randomUUID().toString(),
                false,
                "",
                "Please introduce yourself and summarize your most relevant project.",
                List.of(),
                List.of("Voice interview session created."),
                0,
                now,
                now
        );
        sessions.put(session.sessionId(), session);
        return session;
    }

    public List<VoiceInterviewSession> list() {
        return sessions.values().stream()
                .sorted(Comparator.comparing(VoiceInterviewSession::updatedAt).reversed())
                .toList();
    }

    public VoiceInterviewSession detail(String sessionId) {
        var session = sessions.get(sessionId);
        if (session == null) {
            throw new BizException("Voice interview session not found");
        }
        return session;
    }

    public VoiceInterviewSession pause(String sessionId) {
        return mutate(sessionId, true, null, "Session paused.");
    }

    public VoiceInterviewSession resume(String sessionId) {
        return mutate(sessionId, false, null, "Session resumed.");
    }

    public VoiceInterviewSession submitText(String sessionId, String transcript) {
        var reply = buildAiReply(transcript);
        return mutate(sessionId, false, transcript, reply);
    }

    public Map<String, String> buildRealtimeReply(String sessionId, String payload) {
        var session = detail(sessionId);
        var subtitle = payload == null ? "" : payload.trim();
        var reply = buildAiReply(subtitle);
        var updated = new VoiceInterviewSession(
                session.sessionId(),
                session.paused(),
                subtitle.isBlank() ? session.latestTranscript() : subtitle,
                nextQuestion(session.submittedTurns() + 1),
                append(session.subtitles(), subtitle.isBlank() ? "empty-frame" : subtitle),
                append(session.aiReplies(), reply),
                session.submittedTurns() + (subtitle.isBlank() ? 0 : 1),
                session.createdAt(),
                LocalDateTime.now()
        );
        sessions.put(sessionId, updated);
        return Map.of(
                "subtitle", subtitle,
                "reply", reply,
                "question", updated.currentQuestion()
        );
    }

    public byte[] exportReport(String sessionId) {
        var session = detail(sessionId);
        var body = """
                Session ID: %s
                Paused: %s
                Submitted Turns: %s
                Latest Transcript:
                %s

                Subtitles:
                %s

                AI Replies:
                %s
                """.formatted(
                session.sessionId(),
                session.paused(),
                session.submittedTurns(),
                session.latestTranscript(),
                String.join(" | ", session.subtitles()),
                String.join(" | ", session.aiReplies())
        );
        return pdfExportService.exportTextReport("Voice Interview Report", body);
    }

    private VoiceInterviewSession mutate(String sessionId, boolean paused, String transcript, String aiReply) {
        var session = detail(sessionId);
        var updatedTranscript = transcript == null || transcript.isBlank() ? session.latestTranscript() : transcript;
        var updated = new VoiceInterviewSession(
                session.sessionId(),
                paused,
                updatedTranscript,
                session.currentQuestion(),
                transcript == null ? session.subtitles() : append(session.subtitles(), transcript),
                aiReply == null ? session.aiReplies() : append(session.aiReplies(), aiReply),
                transcript == null || transcript.isBlank() ? session.submittedTurns() : session.submittedTurns() + 1,
                session.createdAt(),
                LocalDateTime.now()
        );
        sessions.put(sessionId, updated);
        return updated;
    }

    private List<String> append(List<String> source, String value) {
        var copy = new ArrayList<>(source);
        copy.add(value);
        return List.copyOf(copy);
    }

    private String buildAiReply(String transcript) {
        if (transcript == null || transcript.isBlank()) {
            return "Please continue. I need more detail about your project and technical choices.";
        }
        var lower = transcript.toLowerCase();
        if (lower.contains("redis") || lower.contains("cache")) {
            return "Good. Why was Redis a better choice than local cache or database reads in that scenario?";
        }
        if (lower.contains("project")) {
            return "Please quantify the project impact with latency, throughput, stability, or delivery metrics.";
        }
        return "Please drill deeper into architecture, trade-offs, and measurable business impact.";
    }

    private String nextQuestion(long turns) {
        if (turns <= 1) {
            return "What was the hardest engineering decision in your recent project?";
        }
        if (turns == 2) {
            return "How did you verify correctness, stability, and observability after release?";
        }
        return "If you had one more sprint, what would you improve and why?";
    }
}
