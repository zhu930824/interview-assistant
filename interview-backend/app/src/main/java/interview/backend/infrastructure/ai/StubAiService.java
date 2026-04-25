package interview.backend.infrastructure.ai;

import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

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

    private String analyzeResumeStub(String resumeText) {
        var name = detectName(resumeText);
        var position = detectPosition(resumeText);
        return String.format("""
                {"candidateName":"%s","targetPosition":"%s","keywords":["java","spring","mysql"],"strengths":["Relevant technical stack coverage"],"risks":["Project impact not quantified"],"summary":"Candidate %s appears aligned with %s."}
                """, name, position, name, position);
    }

    private String evaluateInterviewStub(String transcript) {
        var baseScore = Math.min(85, 50 + transcript.length() / 10);
        var questions = parseQuestionsFromTranscript(transcript);
        var questionScores = new StringBuilder();
        var totalScore = 0;

        for (int i = 0; i < questions.size(); i++) {
            var q = questions.get(i);
            var qScore = calculateQuestionScore(q, i);
            totalScore += qScore;
            questionScores.append(String.format(
                    "{\"questionId\":\"q%d\",\"questionContent\":\"%s\",\"score\":%d,\"comment\":\"%s\"}",
                    i + 1, escapeJson(q.content()), qScore, getComment(qScore)
            ));
            if (i < questions.size() - 1) questionScores.append(",");
        }

        var avgScore = questions.isEmpty() ? baseScore : totalScore / questions.size();
        return String.format("""
                {"totalScore":%d,"questionScores":[%s],"strengths":["回答结构清晰","技术理解到位"],"risks":["项目细节可以更深入","量化指标需要加强"],"feedback":"整体表现良好，建议在项目描述中加入更多可量化的业务指标，并在技术选型时阐述权衡考量。"}
                """, avgScore, questionScores.toString());
    }

    private List<QuestionInfo> parseQuestionsFromTranscript(String transcript) {
        var result = new java.util.ArrayList<QuestionInfo>();
        var parts = transcript.split("Q:");
        for (var part : parts) {
            if (part.isBlank()) continue;
            var qa = part.split("A:", 2);
            if (qa.length >= 1) {
                var question = qa[0].trim();
                var answer = qa.length > 1 ? qa[1].trim() : "";
                result.add(new QuestionInfo(question, answer));
            }
        }
        return result;
    }

    private int calculateQuestionScore(QuestionInfo q, int index) {
        var score = 60;
        var answer = q.answer();
        if (answer.length() > 100) score += 10;
        if (answer.length() > 200) score += 5;
        if (answer.contains("项目") || answer.contains("架构")) score += 8;
        if (answer.contains("优化") || answer.contains("改进")) score += 7;
        if (answer.contains("权衡") || answer.contains("取舍")) score += 10;
        return Math.min(95, score);
    }

    private String getComment(int score) {
        if (score >= 90) return "优秀";
        if (score >= 80) return "良好";
        if (score >= 70) return "合格";
        return "需改进";
    }

    private String escapeJson(String text) {
        return text.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }

    private record QuestionInfo(String content, String answer) {}

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
