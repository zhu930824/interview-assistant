package interview.backend.modules.interview;

import interview.backend.common.exception.BizException;
import interview.backend.infrastructure.export.PdfExportService;
import interview.backend.modules.interview.dto.CreateInterviewSessionRequest;
import interview.backend.modules.interview.model.InterviewDirection;
import interview.backend.modules.interview.model.InterviewQuestion;
import interview.backend.modules.interview.model.InterviewSession;
import interview.backend.modules.interview.model.InterviewStage;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Service;

@Service
public class InterviewService {

    private final AtomicLong idGenerator = new AtomicLong(1);
    private final Map<Long, InterviewSession> sessions = new ConcurrentHashMap<>();
    private final SkillService skillService;
    private final PdfExportService pdfExportService;

    public InterviewService(SkillService skillService, PdfExportService pdfExportService) {
        this.skillService = skillService;
        this.pdfExportService = pdfExportService;
    }

    public InterviewSession create(CreateInterviewSessionRequest request) {
        var id = idGenerator.getAndIncrement();
        var durations = distribute(request.totalMinutes());
        var questions = buildQuestions(request.direction(), request.followUpRounds());
        var now = LocalDateTime.now();
        var session = new InterviewSession(
                id,
                request.direction(),
                request.totalMinutes(),
                durations,
                request.followUpRounds(),
                questions,
                questions.isEmpty() ? null : questions.getFirst().id(),
                "",
                null,
                "IN_PROGRESS",
                now,
                now
        );
        sessions.put(id, session);
        return session;
    }

    public List<InterviewSession> list() {
        return sessions.values().stream()
                .sorted(Comparator.comparing(InterviewSession::updatedAt).reversed())
                .toList();
    }

    public InterviewSession continueSession(Long id) {
        var session = sessions.get(id);
        if (session == null) {
            throw new BizException("Interview session not found");
        }
        return session;
    }

    public InterviewSession answer(Long id, String answer) {
        var session = continueSession(id);
        if (!"IN_PROGRESS".equals(session.status())) {
            throw new BizException("Interview session is not accepting new answers");
        }

        var currentQuestion = currentQuestion(session);
        var updatedTranscript = session.transcript()
                + "\nQ: " + currentQuestion.content()
                + "\nA: " + answer.trim()
                + "\n";

        var nextQuestion = nextQuestion(session);
        var updated = new InterviewSession(
                session.id(),
                session.direction(),
                session.totalMinutes(),
                session.stageDurations(),
                session.followUpRounds(),
                session.askedQuestions(),
                nextQuestion == null ? null : nextQuestion.id(),
                updatedTranscript.trim(),
                session.evaluation(),
                nextQuestion == null ? "READY_FOR_EVALUATION" : "IN_PROGRESS",
                session.createdAt(),
                LocalDateTime.now()
        );
        sessions.put(id, updated);
        return updated;
    }

    public InterviewSession evaluate(Long id, String transcript) {
        var session = continueSession(id);
        var effectiveTranscript = transcript == null || transcript.isBlank() ? session.transcript() : transcript;
        var skillDefinition = skillService.loadDefinition(session.direction());
        var score = calculateScore(effectiveTranscript, session.followUpRounds());
        var strengths = List.of(
                "Response structure is " + (effectiveTranscript.length() > 300 ? "complete" : "still somewhat short"),
                "Relevant to direction: " + session.direction(),
                "Skill checklist loaded successfully"
        );
        var risks = new ArrayList<String>();
        if (effectiveTranscript.length() < 240) {
            risks.add("Need deeper project details and more quantifiable impact.");
        }
        if (!effectiveTranscript.toLowerCase().contains("trade-off")) {
            risks.add("Trade-off thinking did not appear clearly.");
        }
        if (risks.isEmpty()) {
            risks.add("No major weakness detected in the text sample.");
        }

        var evaluation = """
                Overall Score: %s/100
                Direction: %s
                Follow-up Rounds: %s

                Strengths:
                - %s

                Risks:
                - %s

                Skill Reference Preview:
                %s

                Transcript Summary:
                %s
                """.formatted(
                score,
                session.direction(),
                session.followUpRounds(),
                String.join("\n- ", strengths),
                String.join("\n- ", risks),
                preview(skillDefinition),
                preview(effectiveTranscript.isBlank() ? "No transcript submitted." : effectiveTranscript)
        );

        var updated = new InterviewSession(
                session.id(),
                session.direction(),
                session.totalMinutes(),
                session.stageDurations(),
                session.followUpRounds(),
                session.askedQuestions(),
                session.currentQuestionId(),
                effectiveTranscript,
                evaluation,
                "COMPLETED",
                session.createdAt(),
                LocalDateTime.now()
        );
        sessions.put(id, updated);
        return updated;
    }

    public Map<String, Object> centerSummary() {
        var summary = new HashMap<String, Object>();
        summary.put("totalSessions", sessions.size());
        summary.put("completedSessions", sessions.values().stream().filter(session -> "COMPLETED".equals(session.status())).count());
        summary.put("activeSessions", sessions.values().stream().filter(session -> "IN_PROGRESS".equals(session.status())).count());
        summary.put("readyForEvaluation", sessions.values().stream().filter(session -> "READY_FOR_EVALUATION".equals(session.status())).count());
        summary.put("directions", sessions.values().stream().collect(
                java.util.stream.Collectors.groupingBy(InterviewSession::direction, java.util.stream.Collectors.counting())
        ));
        return summary;
    }

    public List<Map<String, String>> directions() {
        var result = new ArrayList<Map<String, String>>();
        for (var direction : InterviewDirection.values()) {
            result.add(Map.of(
                    "code", direction.name(),
                    "label", direction.name().replace('_', ' '),
                    "preview", preview(skillService.loadDefinition(direction))
            ));
        }
        return result;
    }

    public byte[] exportReport(Long id) {
        var session = continueSession(id);
        var body = """
                Direction: %s
                Total Minutes: %s
                Status: %s
                Follow-up Rounds: %s

                Stage Durations:
                %s

                Questions:
                %s

                Transcript:
                %s

                Evaluation:
                %s
                """.formatted(
                session.direction(),
                session.totalMinutes(),
                session.status(),
                session.followUpRounds(),
                session.stageDurations(),
                session.askedQuestions().stream().map(InterviewQuestion::content).toList(),
                session.transcript().isBlank() ? "No transcript yet." : session.transcript(),
                session.evaluation() == null ? "No evaluation yet." : session.evaluation()
        );
        return pdfExportService.exportTextReport("Mock Interview Report", body);
    }

    private Map<InterviewStage, Integer> distribute(int totalMinutes) {
        var result = new LinkedHashMap<InterviewStage, Integer>();
        result.put(InterviewStage.INTRODUCTION, Math.max(3, totalMinutes * 15 / 100));
        result.put(InterviewStage.TECHNICAL, Math.max(6, totalMinutes * 45 / 100));
        result.put(InterviewStage.PROJECT, Math.max(4, totalMinutes * 25 / 100));
        var used = result.values().stream().mapToInt(Integer::intValue).sum();
        result.put(InterviewStage.QA, Math.max(2, totalMinutes - used));
        return result;
    }

    private List<InterviewQuestion> buildQuestions(InterviewDirection direction, int followUpRounds) {
        var skillPreview = preview(skillService.loadDefinition(direction));
        var questions = new ArrayList<InterviewQuestion>();
        questions.add(question(InterviewStage.INTRODUCTION, "Please give a concise self-introduction focused on your recent experience."));
        questions.add(question(InterviewStage.TECHNICAL, "What are the most important skills for " + direction.name().replace('_', ' ') + " and how have you used them?"));
        questions.add(question(InterviewStage.TECHNICAL, "Describe a difficult technical issue you solved and explain the trade-offs you considered."));
        questions.add(question(InterviewStage.PROJECT, "Pick one representative project and walk through goal, architecture, constraints, and measurable impact."));
        questions.add(question(InterviewStage.QA, "If you could ask the interviewer only two questions, what would you ask and why?"));
        for (int i = 1; i <= followUpRounds; i++) {
            questions.add(question(InterviewStage.TECHNICAL, "Follow-up " + i + ": based on the previous answer, what would you improve if you had one more sprint?"));
        }
        if (!skillPreview.isBlank()) {
            questions.add(question(InterviewStage.TECHNICAL, "Skill file focus reminder: " + skillPreview));
        }
        return questions;
    }

    private InterviewQuestion question(InterviewStage stage, String content) {
        return new InterviewQuestion(UUID.randomUUID().toString(), stage, content);
    }

    private InterviewQuestion currentQuestion(InterviewSession session) {
        return session.askedQuestions().stream()
                .filter(question -> question.id().equals(session.currentQuestionId()))
                .findFirst()
                .orElseThrow(() -> new BizException("Current question not found"));
    }

    private InterviewQuestion nextQuestion(InterviewSession session) {
        if (session.currentQuestionId() == null) {
            return null;
        }
        var questions = session.askedQuestions();
        for (int i = 0; i < questions.size(); i++) {
            if (questions.get(i).id().equals(session.currentQuestionId())) {
                return i + 1 >= questions.size() ? null : questions.get(i + 1);
            }
        }
        return null;
    }

    private int calculateScore(String transcript, int followUpRounds) {
        var base = Math.min(65, transcript.length() / 10);
        var tradeOffBonus = transcript.toLowerCase().contains("trade-off") ? 10 : 0;
        var projectBonus = transcript.toLowerCase().contains("project") ? 10 : 0;
        var followUpBonus = Math.min(10, followUpRounds * 3);
        return Math.min(98, base + tradeOffBonus + projectBonus + followUpBonus + 10);
    }

    private String preview(String text) {
        var normalized = text.replaceAll("\\s+", " ").trim();
        if (normalized.length() <= 180) {
            return normalized;
        }
        return normalized.substring(0, 180) + "...";
    }
}
