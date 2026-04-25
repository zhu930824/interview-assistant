package interview.backend.modules.interview;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import interview.backend.common.exception.BizException;
import interview.backend.infrastructure.ai.AiService;
import interview.backend.infrastructure.export.PdfExportService;
import interview.backend.modules.interview.dto.CreateInterviewSessionRequest;
import interview.backend.modules.interview.model.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InterviewService {

    private final InterviewSessionMapper sessionMapper;
    private final InterviewQuestionMapper questionMapper;
    private final SkillService skillService;
    private final PdfExportService pdfExportService;
    private final AiService aiService;

    public InterviewService(
            InterviewSessionMapper sessionMapper,
            InterviewQuestionMapper questionMapper,
            SkillService skillService,
            PdfExportService pdfExportService,
            AiService aiService
    ) {
        this.sessionMapper = sessionMapper;
        this.questionMapper = questionMapper;
        this.skillService = skillService;
        this.pdfExportService = pdfExportService;
        this.aiService = aiService;
    }

    @Transactional
    public InterviewSession create(CreateInterviewSessionRequest request) {
        var direction = InterviewDirection.valueOf(request.direction());
        var durations = distribute(request.totalMinutes());
        var entity = new InterviewSessionEntity();
        entity.setDirection(direction.name());
        entity.setTotalMinutes(request.totalMinutes());
        entity.setStageDurations(durations);
        entity.setFollowUpRounds(request.followUpRounds());
        entity.setStatus("IN_PROGRESS");
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        sessionMapper.insert(entity);

        var questions = buildQuestions(direction, request.followUpRounds());
        for (int i = 0; i < questions.size(); i++) {
            var q = questions.get(i);
            var qEntity = InterviewQuestionEntity.create(q.id(), entity.getId(), q.stage().name(), q.content(), i);
            questionMapper.insert(qEntity);
        }

        entity.setCurrentQuestionId(questions.isEmpty() ? null : questions.getFirst().id());
        sessionMapper.updateById(entity);
        return toSession(entity, questions);
    }

    public List<InterviewSession> list() {
        return sessionMapper.selectList(
                new QueryWrapper<InterviewSessionEntity>().orderByDesc("updated_at")
        ).stream()
                .map(e -> toSession(e, loadQuestions(e.getId())))
                .toList();
    }

    public InterviewSession continueSession(Long id) {
        var entity = sessionMapper.selectById(id);
        if (entity == null) {
            throw new BizException("Interview session not found");
        }
        return toSession(entity, loadQuestions(id));
    }

    @Transactional
    public InterviewSession answer(Long id, String answer) {
        var entity = sessionMapper.selectById(id);
        if (entity == null) {
            throw new BizException("Interview session not found");
        }
        if (!"IN_PROGRESS".equals(entity.getStatus())) {
            throw new BizException("Interview session is not accepting new answers");
        }

        var questions = loadQuestions(id);
        var currentQuestion = questions.stream()
                .filter(q -> q.id().equals(entity.getCurrentQuestionId()))
                .findFirst()
                .orElseThrow(() -> new BizException("Current question not found"));

        var updatedTranscript = (entity.getTranscript() == null ? "" : entity.getTranscript())
                + "\nQ: " + currentQuestion.content()
                + "\nA: " + answer.trim()
                + "\n";

        var nextQuestion = findNextQuestion(questions, entity.getCurrentQuestionId());

        entity.setTranscript(updatedTranscript.trim());
        entity.setCurrentQuestionId(nextQuestion == null ? null : nextQuestion.id());
        entity.setStatus(nextQuestion == null ? "READY_FOR_EVALUATION" : "IN_PROGRESS");
        entity.setUpdatedAt(LocalDateTime.now());
        sessionMapper.updateById(entity);

        return toSession(entity, questions);
    }

    @Transactional
    public InterviewSession evaluate(Long id, String transcript) {
        var entity = sessionMapper.selectById(id);
        if (entity == null) {
            throw new BizException("Interview session not found");
        }
        var effectiveTranscript = transcript == null || transcript.isBlank() ? entity.getTranscript() : transcript;

        var direction = InterviewDirection.valueOf(entity.getDirection());
        var skillDefinition = skillService.loadDefinition(direction);
        var aiResponse = aiService.chat(
                "You are an interviewer evaluating candidate responses. Skill reference: " + skillDefinition,
                "Evaluate this interview transcript:\n" + effectiveTranscript
        );

        var evaluation = parseEvaluation(aiResponse, entity, effectiveTranscript);

        entity.setTranscript(effectiveTranscript);
        entity.setEvaluation(evaluation);
        entity.setStatus("COMPLETED");
        entity.setUpdatedAt(LocalDateTime.now());
        sessionMapper.updateById(entity);

        return toSession(entity, loadQuestions(id));
    }

    public Map<String, Object> centerSummary() {
        var total = sessionMapper.selectCount(null);
        var completed = sessionMapper.selectCount(
                new QueryWrapper<InterviewSessionEntity>().eq("status", "COMPLETED")
        );
        var active = sessionMapper.selectCount(
                new QueryWrapper<InterviewSessionEntity>().eq("status", "IN_PROGRESS")
        );
        var ready = sessionMapper.selectCount(
                new QueryWrapper<InterviewSessionEntity>().eq("status", "READY_FOR_EVALUATION")
        );
        var directions = sessionMapper.selectList(null).stream()
                .collect(Collectors.groupingBy(InterviewSessionEntity::getDirection, Collectors.counting()));
        return Map.of(
                "totalSessions", total,
                "completedSessions", completed,
                "activeSessions", active,
                "readyForEvaluation", ready,
                "directions", directions
        );
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
                session.transcript() == null || session.transcript().isBlank() ? "No transcript yet." : session.transcript(),
                session.evaluation() == null ? "No evaluation yet." : session.evaluation()
        );
        return pdfExportService.exportTextReport("Mock Interview Report", body);
    }

    private List<InterviewQuestion> loadQuestions(Long sessionId) {
        return questionMapper.findBySessionIdOrderBySortOrder(sessionId).stream()
                .map(e -> new InterviewQuestion(
                        e.getId(),
                        InterviewStage.valueOf(e.getStage()),
                        e.getContent()
                ))
                .toList();
    }

    private InterviewSession toSession(InterviewSessionEntity entity, List<InterviewQuestion> questions) {
        var stageDurations = entity.getStageDurations() != null
                ? entity.getStageDurations().entrySet().stream()
                .collect(Collectors.toMap(
                        e -> InterviewStage.valueOf(e.getKey()),
                        Map.Entry::getValue,
                        (a, b) -> a,
                        LinkedHashMap::new
                ))
                : Map.<InterviewStage, Integer>of();

        return new InterviewSession(
                entity.getId(),
                InterviewDirection.valueOf(entity.getDirection()),
                entity.getTotalMinutes(),
                stageDurations,
                entity.getFollowUpRounds(),
                questions,
                entity.getCurrentQuestionId(),
                entity.getTranscript() != null ? entity.getTranscript() : "",
                entity.getEvaluation(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private InterviewQuestion findNextQuestion(List<InterviewQuestion> questions, String currentId) {
        if (currentId == null) return null;
        for (int i = 0; i < questions.size(); i++) {
            if (questions.get(i).id().equals(currentId)) {
                return i + 1 >= questions.size() ? null : questions.get(i + 1);
            }
        }
        return null;
    }

    private Map<String, Integer> distribute(int totalMinutes) {
        var result = new LinkedHashMap<String, Integer>();
        result.put("INTRODUCTION", Math.max(3, totalMinutes * 15 / 100));
        result.put("TECHNICAL", Math.max(6, totalMinutes * 45 / 100));
        result.put("PROJECT", Math.max(4, totalMinutes * 25 / 100));
        var used = result.values().stream().mapToInt(Integer::intValue).sum();
        result.put("QA", Math.max(2, totalMinutes - used));
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

    private String parseEvaluation(String aiResponse, InterviewSessionEntity entity, String transcript) {
        if (aiResponse != null && !aiResponse.isBlank()) {
            return aiResponse;
        }
        var score = Math.min(65, transcript.length() / 10) + 10;
        var tradeOff = transcript.toLowerCase().contains("trade-off") ? 10 : 0;
        var project = transcript.toLowerCase().contains("project") ? 10 : 0;
        return """
                Overall Score: %d/100
                Direction: %s
                Follow-up Rounds: %s

                Strengths:
                - Response structure is %s
                - Relevant to direction: %s

                Risks:
                - %s
                """.formatted(
                Math.min(98, score + tradeOff + project),
                entity.getDirection(),
                entity.getFollowUpRounds(),
                transcript.length() > 300 ? "complete" : "still somewhat short",
                entity.getDirection(),
                transcript.length() < 240 ? "Need deeper project details and more quantifiable impact." : "No major weakness detected in the text sample."
        );
    }

    private String preview(String text) {
        var normalized = text.replaceAll("\\s+", " ").trim();
        if (normalized.length() <= 180) {
            return normalized;
        }
        return normalized.substring(0, 180) + "...";
    }
}
