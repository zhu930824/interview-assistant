package interview.backend.modules.resume.model;

import java.time.LocalDateTime;
import java.util.List;

public record ResumeRecord(
        Long id,
        String fileName,
        String filePath,
        String contentHash,
        ResumeStatus status,
        int retryCount,
        int progress,
        boolean duplicated,
        String candidateName,
        String targetPosition,
        String analysisSummary,
        List<String> keywords,
        List<String> strengths,
        List<String> risks,
        String rawTextPreview,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
