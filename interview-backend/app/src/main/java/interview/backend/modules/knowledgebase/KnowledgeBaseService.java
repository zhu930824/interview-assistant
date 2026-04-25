package interview.backend.modules.knowledgebase;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import interview.backend.common.exception.BizException;
import interview.backend.infrastructure.ai.AiService;
import interview.backend.infrastructure.file.DocumentTextExtractor;
import interview.backend.infrastructure.storage.LocalStorageService;
import interview.backend.modules.knowledgebase.model.DocumentEntity;
import interview.backend.modules.knowledgebase.model.KnowledgeDocument;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

@Service
public class KnowledgeBaseService {

    private final DocumentMapper documentMapper;
    private final LocalStorageService storageService;
    private final DocumentTextExtractor textExtractor;
    private final AiService aiService;

    public KnowledgeBaseService(
            DocumentMapper documentMapper,
            LocalStorageService storageService,
            DocumentTextExtractor textExtractor,
            AiService aiService
    ) {
        this.documentMapper = documentMapper;
        this.storageService = storageService;
        this.textExtractor = textExtractor;
        this.aiService = aiService;
    }

    public KnowledgeDocument upload(MultipartFile file) {
        try {
            var filePath = storageService.save(file, "knowledge");
            var fileName = file.getOriginalFilename() == null ? "knowledge.txt" : file.getOriginalFilename();
            var content = textExtractor.extract(file.getInputStream(), fileName);

            var entity = new DocumentEntity();
            entity.setFileName(fileName);
            entity.setFilePath(filePath);
            entity.setSummary(summarize(content));
            entity.setChunks(Math.max(1, (int) Math.ceil(Math.max(content.length(), 1) / 500.0)));
            entity.setTokenEstimate(Math.max(1, content.length() / 4));
            entity.setKeywords(extractKeywords(content));
            entity.setFullContent(content);
            entity.setCreatedAt(LocalDateTime.now());
            entity.setUpdatedAt(LocalDateTime.now());
            documentMapper.insert(entity);

            return toDocument(entity);
        } catch (IOException ex) {
            throw new BizException("Knowledge document upload failed: " + ex.getMessage());
        }
    }

    public List<KnowledgeDocument> list() {
        return documentMapper.selectList(
                new QueryWrapper<DocumentEntity>().orderByDesc("created_at")
        ).stream()
                .map(this::toDocument)
                .toList();
    }

    public Map<String, Object> stats() {
        return Map.of(
                "totalDocuments", documentMapper.selectCount(null),
                "totalChunks", documentMapper.selectList(null).stream().mapToInt(DocumentEntity::getChunks).sum(),
                "totalTokens", documentMapper.selectList(null).stream().mapToInt(DocumentEntity::getTokenEstimate).sum()
        );
    }

    public SseEmitter chat(String question) {
        var emitter = new SseEmitter(60_000L);
        Thread.startVirtualThread(() -> {
            try {
                var matches = search(question);
                emitter.send(SseEmitter.event().name("message").data("Searching knowledge base for: " + question));
                emitter.send(SseEmitter.event().name("message").data("Matched documents: " + matches.stream().map(KnowledgeDocument::fileName).toList()));
                if (matches.isEmpty()) {
                    emitter.send(SseEmitter.event().name("message").data("No direct hit found. Try uploading more relevant docs or refining the question."));
                    emitter.complete();
                    return;
                }

                var contexts = new ArrayList<String>();
                for (var match : matches) {
                    var entity = documentMapper.selectById(match.id());
                    if (entity != null && entity.getFullContent() != null) {
                        contexts.add(entity.getFullContent());
                        emitter.send(SseEmitter.event().name("message").data("Using " + match.fileName() + ": " + match.summary()));
                    }
                }

                var aiAnswer = aiService.chat(
                        "You are a helpful assistant. Answer questions based on the provided context.",
                        "Context:\n" + String.join("\n---\n", contexts) + "\n\nQuestion: " + question
                );
                emitter.send(SseEmitter.event().name("message").data("Answer: " + aiAnswer));
                emitter.complete();
            } catch (Exception ex) {
                emitter.completeWithError(ex);
            }
        });
        return emitter;
    }

    public Flux<String> chatStream(String question) {
        return aiService.chatStream(
                "You are a helpful assistant.",
                question
        );
    }

    public List<KnowledgeDocument> search(String question) {
        var normalizedQuestion = question == null ? "" : question.toLowerCase();
        return documentMapper.selectList(null).stream()
                .sorted(Comparator.comparingInt((DocumentEntity doc) -> relevance(normalizedQuestion, doc)).reversed())
                .filter(doc -> relevance(normalizedQuestion, doc) > 0)
                .limit(3)
                .map(this::toDocument)
                .toList();
    }

    public byte[] download(Long id) {
        var entity = documentMapper.selectById(id);
        if (entity == null) {
            throw new BizException("Knowledge document not found");
        }
        try {
            return Files.readAllBytes(Path.of(entity.getFilePath()));
        } catch (IOException ex) {
            throw new BizException("Knowledge document download failed: " + ex.getMessage());
        }
    }

    private int relevance(String question, DocumentEntity document) {
        var score = 0;
        var keywords = document.getKeywords();
        if (keywords != null) {
            for (var keyword : keywords) {
                if (question.contains(keyword.toLowerCase())) {
                    score += 2;
                }
            }
        }
        var content = document.getFullContent();
        if (content != null) {
            var lowerContent = content.toLowerCase();
            for (var token : question.split("\\s+")) {
                if (!token.isBlank() && lowerContent.contains(token)) {
                    score++;
                }
            }
        }
        return score;
    }

    private KnowledgeDocument toDocument(DocumentEntity entity) {
        return new KnowledgeDocument(
                entity.getId(),
                entity.getFileName(),
                entity.getSummary(),
                entity.getChunks(),
                entity.getTokenEstimate(),
                entity.getKeywords() != null ? entity.getKeywords() : List.of(),
                entity.getCreatedAt()
        );
    }

    private List<String> extractKeywords(String content) {
        var result = new LinkedHashSet<String>();
        var lower = content.toLowerCase();
        for (var keyword : List.of(
                "java", "spring", "mysql", "redis", "elasticsearch", "vector", "rag",
                "agent", "vue", "typescript", "python", "docker", "kubernetes", "testing"
        )) {
            if (lower.contains(keyword)) {
                result.add(keyword);
            }
        }
        if (result.isEmpty()) {
            result.add("general");
        }
        return new ArrayList<>(result);
    }

    private String summarize(String content) {
        var normalized = content == null ? "" : content.replaceAll("\\s+", " ").trim();
        if (normalized.length() <= 160) {
            return normalized;
        }
        return normalized.substring(0, 160) + "...";
    }
}
