package interview.backend.modules.interview.dto;

import jakarta.validation.constraints.NotBlank;

public record InterviewAnswerRequest(
        @NotBlank String answer
) {
}
