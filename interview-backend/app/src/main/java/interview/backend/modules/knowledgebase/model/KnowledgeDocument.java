package interview.backend.modules.knowledgebase.model;

import java.time.LocalDateTime;
import java.util.List;

public record KnowledgeDocument(
        Long id,
        String fileName,
        String summary,
        int chunks,
        int tokenEstimate,
        List<String> keywords,
        LocalDateTime createdAt
) {
}
