package interview.backend.modules.voiceinterview;

import interview.backend.common.exception.BizException;
import interview.backend.infrastructure.ai.AiService;
import interview.backend.infrastructure.export.PdfExportService;
import interview.backend.modules.voiceinterview.model.VoiceSessionEntity;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class VoiceInterviewService {

    private final VoiceSessionMapper sessionMapper;
    private final PdfExportService pdfExportService;
    private final AiService aiService;

    public VoiceInterviewService(
            VoiceSessionMapper sessionMapper,
            PdfExportService pdfExportService,
            AiService aiService
    ) {
        this.sessionMapper = sessionMapper;
        this.pdfExportService = pdfExportService;
        this.aiService = aiService;
    }

    public VoiceInterviewSession create() {
        var now = LocalDateTime.now();
        var entity = new VoiceSessionEntity();
        entity.setSessionId(UUID.randomUUID().toString());
        entity.setPaused(false);
        entity.setLatestTranscript("");
        entity.setCurrentQuestion("Please introduce yourself and summarize your most relevant project.");
        entity.setSubtitles(List.of());
        entity.setAiReplies(List.of("Voice interview session created."));
        entity.setSubmittedTurns(0);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        sessionMapper.insert(entity);
        return toSession(entity);
    }

    public List<VoiceInterviewSession> list() {
        return sessionMapper.selectList(null).stream()
                .sorted(Comparator.comparing(VoiceSessionEntity::getUpdatedAt).reversed())
                .map(this::toSession)
                .toList();
    }

    public VoiceInterviewSession detail(String sessionId) {
        var entity = sessionMapper.selectById(sessionId);
        if (entity == null) {
            throw new BizException("Voice interview session not found");
        }
        return toSession(entity);
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
        var entity = sessionMapper.selectById(sessionId);
        if (entity == null) {
            throw new BizException("Voice interview session not found");
        }
        var subtitle = payload == null ? "" : payload.trim();
        var reply = buildAiReply(subtitle);
        entity.setPaused(false);
        if (!subtitle.isBlank()) {
            entity.setLatestTranscript(subtitle);
        }
        entity.setCurrentQuestion(nextQuestion(entity.getSubmittedTurns() + (subtitle.isBlank() ? 0 : 1)));
        entity.setSubtitles(append(entity.getSubtitles(), subtitle.isBlank() ? "empty-frame" : subtitle));
        entity.setAiReplies(append(entity.getAiReplies(), reply));
        entity.setSubmittedTurns(entity.getSubmittedTurns() + (subtitle.isBlank() ? 0 : 1));
        entity.setUpdatedAt(LocalDateTime.now());
        sessionMapper.updateById(entity);
        return Map.of(
                "subtitle", subtitle,
                "reply", reply,
                "question", entity.getCurrentQuestion()
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
        var entity = sessionMapper.selectById(sessionId);
        if (entity == null) {
            throw new BizException("Voice interview session not found");
        }
        var updatedTranscript = transcript == null || transcript.isBlank()
                ? entity.getLatestTranscript()
                : transcript;
        entity.setPaused(paused);
        entity.setLatestTranscript(updatedTranscript);
        if (transcript != null && !transcript.isBlank()) {
            entity.setSubtitles(append(entity.getSubtitles(), transcript));
        }
        if (aiReply != null && !aiReply.isBlank()) {
            entity.setAiReplies(append(entity.getAiReplies(), aiReply));
        }
        entity.setSubmittedTurns(transcript == null || transcript.isBlank()
                ? entity.getSubmittedTurns()
                : entity.getSubmittedTurns() + 1);
        entity.setUpdatedAt(LocalDateTime.now());
        sessionMapper.updateById(entity);
        return toSession(entity);
    }

    private String buildAiReply(String transcript) {
        if (transcript == null || transcript.isBlank()) {
            return "Please continue. I need more detail about your project and technical choices.";
        }
        var aiResponse = aiService.chat(
                "You are a voice interview coach. Provide brief, encouraging follow-up questions.",
                "Candidate response: " + transcript
        );
        if (aiResponse != null && !aiResponse.isBlank()) {
            return aiResponse;
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

    private List<String> append(List<String> source, String value) {
        var copy = new ArrayList<>(source != null ? source : List.of());
        copy.add(value);
        return List.copyOf(copy);
    }

    private VoiceInterviewSession toSession(VoiceSessionEntity entity) {
        return new VoiceInterviewSession(
                entity.getSessionId(),
                Boolean.TRUE.equals(entity.getPaused()),
                entity.getLatestTranscript() != null ? entity.getLatestTranscript() : "",
                entity.getCurrentQuestion() != null ? entity.getCurrentQuestion() : "",
                entity.getSubtitles() != null ? entity.getSubtitles() : List.of(),
                entity.getAiReplies() != null ? entity.getAiReplies() : List.of(),
                entity.getSubmittedTurns() != null ? entity.getSubmittedTurns() : 0,
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
