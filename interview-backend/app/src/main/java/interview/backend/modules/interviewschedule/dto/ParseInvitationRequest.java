package interview.backend.modules.interviewschedule.dto;

import jakarta.validation.constraints.NotBlank;

public record ParseInvitationRequest(
        @NotBlank String content
) {
}
