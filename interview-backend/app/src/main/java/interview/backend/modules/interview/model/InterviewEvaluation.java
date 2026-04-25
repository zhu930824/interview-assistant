package interview.backend.modules.interview.model;

import java.util.List;

public record InterviewEvaluation(
        int totalScore,
        List<QuestionScore> questionScores,
        List<String> strengths,
        List<String> risks,
        String feedback
) {
    public record QuestionScore(
            String questionId,
            String questionContent,
            int score,
            String comment
    ) {}
}