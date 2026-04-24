package interview.backend.modules.interview.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record InterviewSession(
        Long id,
        InterviewDirection direction,
        int totalMinutes,
        Map<InterviewStage, Integer> stageDurations,
        int followUpRounds,
        List<InterviewQuestion> askedQuestions,
        String currentQuestionId,
        String transcript,
        String evaluation,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
