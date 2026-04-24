package interview.backend.modules.knowledgebase;

import interview.backend.common.exception.BizException;
import interview.backend.infrastructure.file.DocumentTextExtractor;
import interview.backend.infrastructure.storage.LocalStorageService;
import interview.backend.modules.knowledgebase.model.KnowledgeDocument;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
public class KnowledgeBaseService {

    private final AtomicLong idGenerator = new AtomicLong(1);
    private final Map<Long, KnowledgeDocument> documents = new ConcurrentHashMap<>();
    private final Map<Long, String> filePaths = new ConcurrentHashMap<>();
    private final Map<Long, String> documentContent = new ConcurrentHashMap<>();
    private final LocalStorageService storageService;
    private final DocumentTextExtractor textExtractor;

    public KnowledgeBaseService(LocalStorageService storageService, DocumentTextExtractor textExtractor) {
        this.storageService = storageService;
        this.textExtractor = textExtractor;
    }

    public KnowledgeDocument upload(MultipartFile file) {
        try {
            var filePath = storageService.save(file, "knowledge");
            var fileName = file.getOriginalFilename() == null ? "knowledge.txt" : file.getOriginalFilename();
            var content = textExtractor.extract(file.getInputStream(), fileName);
            var chunks = Math.max(1, (int) Math.ceil(Math.max(content.length(), 1) / 500.0));
            var keywords = extractKeywords(content);
            var document = new KnowledgeDocument(
                    idGenerator.getAndIncrement(),
                    fileName,
                    summarize(content),
                    chunks,
                    Math.max(1, content.length() / 4),
                    keywords,
                    LocalDateTime.now()
            );
            documents.put(document.id(), document);
            filePaths.put(document.id(), filePath);
            documentContent.put(document.id(), content);
            return document;
        } catch (IOException ex) {
            throw new BizException("Knowledge document upload failed: " + ex.getMessage());
        }
    }

    public List<KnowledgeDocument> list() {
        return documents.values().stream()
                .sorted(Comparator.comparing(KnowledgeDocument::createdAt).reversed())
                .toList();
    }

    public Map<String, Object> stats() {
        var result = new java.util.LinkedHashMap<String, Object>();
        result.put("totalDocuments", documents.size());
        result.put("totalChunks", documents.values().stream().mapToInt(KnowledgeDocument::chunks).sum());
        result.put("totalTokens", documents.values().stream().mapToInt(KnowledgeDocument::tokenEstimate).sum());
        return result;
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

                for (var match : matches) {
                    var content = documentContent.getOrDefault(match.id(), "");
                    emitter.send(SseEmitter.event().name("message").data("Using " + match.fileName() + ": " + summarize(content)));
                }

                emitter.send(SseEmitter.event().name("message").data("Answer: " + synthesizeAnswer(question, matches)));
                emitter.complete();
            } catch (Exception ex) {
                emitter.completeWithError(ex);
            }
        });
        return emitter;
    }

    public List<KnowledgeDocument> search(String question) {
        var normalizedQuestion = question == null ? "" : question.toLowerCase();
        return documents.values().stream()
                .sorted(Comparator.comparingInt((KnowledgeDocument doc) -> relevance(normalizedQuestion, doc)).reversed())
                .filter(doc -> relevance(normalizedQuestion, doc) > 0)
                .limit(3)
                .toList();
    }

    public byte[] download(Long id) {
        var path = filePaths.get(id);
        if (path == null) {
            throw new BizException("Knowledge document not found");
        }
        try {
            return Files.readAllBytes(Path.of(path));
        } catch (IOException ex) {
            throw new BizException("Knowledge document download failed: " + ex.getMessage());
        }
    }

    private int relevance(String question, KnowledgeDocument document) {
        var score = 0;
        for (var keyword : document.keywords()) {
            if (question.contains(keyword.toLowerCase())) {
                score += 2;
            }
        }
        var content = documentContent.getOrDefault(document.id(), "").toLowerCase();
        for (var token : question.split("\\s+")) {
            if (!token.isBlank() && content.contains(token)) {
                score++;
            }
        }
        return score;
    }

    private String synthesizeAnswer(String question, List<KnowledgeDocument> matches) {
        var snippets = new ArrayList<String>();
        for (var match : matches) {
            snippets.add(match.fileName() + ": " + match.summary());
        }
        return "Question `" + question + "` is most related to " + String.join(" | ", snippets)
                + ". In a real deployment this response can be replaced by Spring AI + vector retrieval + model generation.";
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
