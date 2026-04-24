package interview.backend.modules.interview.dto;

import interview.backend.modules.interview.model.InterviewDirection;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CreateInterviewSessionRequest(
        @NotNull InterviewDirection direction,
        @Min(10) @Max(180) int totalMinutes,
        @Min(0) @Max(5) int followUpRounds
) {
}
