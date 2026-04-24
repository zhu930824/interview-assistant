package interview.backend.modules.interview.model;

public record InterviewQuestion(
        String id,
        InterviewStage stage,
        String content
) {
}
