package interview.backend.modules.resume.dto;

import interview.backend.modules.resume.model.ResumeStatus;

public record ResumeUploadResponse(
        Long id,
        ResumeStatus status,
        boolean duplicated
) {
}
