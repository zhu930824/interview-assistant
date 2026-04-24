package interview.backend.modules.resume;

import interview.backend.common.exception.BizException;
import interview.backend.infrastructure.export.PdfExportService;
import interview.backend.infrastructure.file.DocumentTextExtractor;
import interview.backend.infrastructure.redis.RedisStreamService;
import interview.backend.infrastructure.storage.LocalStorageService;
import interview.backend.modules.resume.dto.ResumeUploadResponse;
import interview.backend.modules.resume.model.ResumeRecord;
import interview.backend.modules.resume.model.ResumeStatus;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ResumeService {

    private static final int PREVIEW_LIMIT = 240;

    private final AtomicLong idGenerator = new AtomicLong(1);
    private final Map<Long, ResumeRecord> resumes = new ConcurrentHashMap<>();
    private final Map<String, Long> hashIndex = new ConcurrentHashMap<>();
    private final LocalStorageService storageService;
    private final DocumentTextExtractor textExtractor;
    private final PdfExportService pdfExportService;
    private final RedisStreamService redisStreamService;
    private final int maxRetry;

    public ResumeService(
            LocalStorageService storageService,
            DocumentTextExtractor textExtractor,
            PdfExportService pdfExportService,
            RedisStreamService redisStreamService,
            @Value("${app.resume.max-retry:3}") int maxRetry
    ) {
        this.storageService = storageService;
        this.textExtractor = textExtractor;
        this.pdfExportService = pdfExportService;
        this.redisStreamService = redisStreamService;
        this.maxRetry = maxRetry;
    }

    public ResumeUploadResponse upload(MultipartFile file) {
        validateFile(file);
        try {
            var bytes = file.getBytes();
            var fileName = sanitizeFileName(file.getOriginalFilename());
            var hash = sha256(bytes);
            var duplicatedId = hashIndex.get(hash);
            if (duplicatedId != null) {
                var duplicated = detail(duplicatedId);
                return new ResumeUploadResponse(duplicated.id(), duplicated.status(), true);
            }

            var filePath = storageService.save(file, "resume");
            var now = LocalDateTime.now();
            var id = idGenerator.getAndIncrement();
            var record = new ResumeRecord(
                    id,
                    fileName,
                    filePath,
                    hash,
                    ResumeStatus.PENDING,
                    0,
                    5,
                    false,
                    "Unknown Candidate",
                    guessTargetPosition(fileName),
                    "Queued for resume analysis.",
                    List.of(),
                    List.of(),
                    List.of(),
                    "",
                    now,
                    now
            );
            resumes.put(id, record);
            hashIndex.put(hash, id);
            redisStreamService.publish("resume-analysis", String.valueOf(id), "queued");
            startAnalysis(id, bytes, fileName);
            return new ResumeUploadResponse(id, ResumeStatus.PENDING, false);
        } catch (IOException ex) {
            throw new BizException("Resume upload failed: " + ex.getMessage());
        }
    }

    public List<ResumeRecord> list() {
        return resumes.values().stream()
                .sorted(Comparator.comparing(ResumeRecord::updatedAt).reversed())
                .toList();
    }

    public ResumeRecord detail(Long id) {
        var record = resumes.get(id);
        if (record == null) {
            throw new BizException("Resume record not found");
        }
        return record;
    }

    public ResumeRecord retry(Long id) {
        var record = detail(id);
        if (record.retryCount() >= maxRetry) {
            throw new BizException("Retry limit reached");
        }
        try {
            var fileBytes = Files.readAllBytes(Path.of(record.filePath()));
            var pending = new ResumeRecord(
                    record.id(),
                    record.fileName(),
                    record.filePath(),
                    record.contentHash(),
                    ResumeStatus.PENDING,
                    record.retryCount() + 1,
                    5,
                    record.duplicated(),
                    record.candidateName(),
                    record.targetPosition(),
                    "Requeued for analysis.",
                    record.keywords(),
                    record.strengths(),
                    record.risks(),
                    record.rawTextPreview(),
                    record.createdAt(),
                    LocalDateTime.now()
            );
            resumes.put(id, pending);
            redisStreamService.publish("resume-analysis", String.valueOf(id), "retry");
            startAnalysis(id, fileBytes, record.fileName());
            return pending;
        } catch (IOException ex) {
            throw new BizException("Retry failed: " + ex.getMessage());
        }
    }

    public Map<String, Object> stats() {
        var counts = resumes.values().stream()
                .collect(java.util.stream.Collectors.groupingBy(ResumeRecord::status, java.util.stream.Collectors.counting()));
        var result = new java.util.LinkedHashMap<String, Object>();
        result.put("total", resumes.size());
        result.put("pending", counts.getOrDefault(ResumeStatus.PENDING, 0L));
        result.put("analyzing", counts.getOrDefault(ResumeStatus.ANALYZING, 0L));
        result.put("completed", counts.getOrDefault(ResumeStatus.COMPLETED, 0L));
        result.put("failed", counts.getOrDefault(ResumeStatus.FAILED, 0L));
        return result;
    }

    public byte[] exportReport(Long id) {
        var record = detail(id);
        var body = """
                Candidate: %s
                Target Position: %s
                File Name: %s
                Status: %s
                Retry Count: %s

                Summary:
                %s

                Keywords:
                %s

                Strengths:
                %s

                Risks:
                %s

                Preview:
                %s
                """.formatted(
                record.candidateName(),
                record.targetPosition(),
                record.fileName(),
                record.status(),
                record.retryCount(),
                record.analysisSummary(),
                String.join(", ", record.keywords()),
                String.join("; ", record.strengths()),
                String.join("; ", record.risks()),
                record.rawTextPreview()
        );
        return pdfExportService.exportTextReport("Resume Analysis Report", body);
    }

    private void startAnalysis(Long id, byte[] fileBytes, String fileName) {
        Thread.startVirtualThread(() -> analyze(id, fileBytes, fileName));
    }

    private void analyze(Long id, byte[] fileBytes, String fileName) {
        try {
            updateStatus(id, ResumeStatus.ANALYZING, 20, "Parsing resume content...");
            redisStreamService.publish("resume-analysis", String.valueOf(id), "analyzing");

            var text = textExtractor.extract(new ByteArrayInputStream(fileBytes), fileName);
            if (text.isBlank()) {
                throw new BizException("No readable text found in the uploaded document");
            }

            updateStatus(id, ResumeStatus.ANALYZING, 55, "Extracting profile and skill signals...");
            var name = detectCandidateName(text, fileName);
            var position = detectTargetPosition(text, fileName);
            var keywords = extractKeywords(text);
            var strengths = buildStrengths(text, keywords);
            var risks = buildRisks(text);
            var summary = buildSummary(name, position, keywords, strengths, risks);

            var current = detail(id);
            var completed = new ResumeRecord(
                    current.id(),
                    current.fileName(),
                    current.filePath(),
                    current.contentHash(),
                    ResumeStatus.COMPLETED,
                    current.retryCount(),
                    100,
                    current.duplicated(),
                    name,
                    position,
                    summary,
                    keywords,
                    strengths,
                    risks,
                    preview(text),
                    current.createdAt(),
                    LocalDateTime.now()
            );
            resumes.put(id, completed);
            redisStreamService.publish("resume-analysis", String.valueOf(id), "completed");
        } catch (Exception ex) {
            var current = detail(id);
            var failed = new ResumeRecord(
                    current.id(),
                    current.fileName(),
                    current.filePath(),
                    current.contentHash(),
                    ResumeStatus.FAILED,
                    current.retryCount(),
                    current.progress(),
                    current.duplicated(),
                    current.candidateName(),
                    current.targetPosition(),
                    "Analysis failed: " + ex.getMessage(),
                    current.keywords(),
                    current.strengths(),
                    current.risks(),
                    current.rawTextPreview(),
                    current.createdAt(),
                    LocalDateTime.now()
            );
            resumes.put(id, failed);
            redisStreamService.publish("resume-analysis", String.valueOf(id), "failed");
        }
    }

    private void updateStatus(Long id, ResumeStatus status, int progress, String summary) {
        var current = detail(id);
        resumes.put(id, new ResumeRecord(
                current.id(),
                current.fileName(),
                current.filePath(),
                current.contentHash(),
                status,
                current.retryCount(),
                progress,
                current.duplicated(),
                current.candidateName(),
                current.targetPosition(),
                summary,
                current.keywords(),
                current.strengths(),
                current.risks(),
                current.rawTextPreview(),
                current.createdAt(),
                LocalDateTime.now()
        ));
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BizException("Please select a resume file");
        }
    }

    private String sanitizeFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return "resume.txt";
        }
        return fileName.replace("\\", "_").replace("/", "_");
    }

    private String sha256(byte[] bytes) {
        try {
            var digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(bytes));
        } catch (Exception ex) {
            throw new BizException("Hash calculation failed");
        }
    }

    private String detectCandidateName(String text, String fileName) {
        var lines = text.lines()
                .map(String::trim)
                .filter(line -> !line.isBlank())
                .limit(8)
                .toList();
        for (var line : lines) {
            if (line.length() >= 2 && line.length() <= 40 && line.chars().noneMatch(Character::isDigit)) {
                return line;
            }
        }
        return fileName.replaceFirst("\\.[^.]+$", "");
    }

    private String detectTargetPosition(String text, String fileName) {
        var lower = (fileName + " " + text).toLowerCase();
        if (lower.contains("java")) {
            return "Java Backend Engineer";
        }
        if (lower.contains("frontend") || lower.contains("vue") || lower.contains("react")) {
            return "Frontend Engineer";
        }
        if (lower.contains("python")) {
            return "Python Engineer";
        }
        if (lower.contains("algorithm")) {
            return "Algorithm Engineer";
        }
        if (lower.contains("ai")) {
            return "AI Engineer";
        }
        return "General Software Engineer";
    }

    private String guessTargetPosition(String fileName) {
        return detectTargetPosition("", Objects.requireNonNullElse(fileName, ""));
    }

    private List<String> extractKeywords(String text) {
        Set<String> ordered = new LinkedHashSet<>();
        var lower = text.toLowerCase();
        for (var candidate : List.of(
                "java", "spring", "spring boot", "mysql", "redis", "elasticsearch", "kafka",
                "vue", "react", "typescript", "python", "docker", "kubernetes", "ai",
                "rag", "agent", "linux", "microservice", "system design", "algorithm"
        )) {
            if (lower.contains(candidate)) {
                ordered.add(candidate);
            }
        }
        if (ordered.isEmpty()) {
            ordered.add("communication");
            ordered.add("project delivery");
        }
        return new ArrayList<>(ordered);
    }

    private List<String> buildStrengths(String text, List<String> keywords) {
        var strengths = new ArrayList<String>();
        if (!keywords.isEmpty()) {
            strengths.add("Relevant technical stack coverage: " + String.join(", ", keywords));
        }
        if (text.toLowerCase().contains("project")) {
            strengths.add("Includes project-oriented experience.");
        }
        if (text.toLowerCase().contains("leader") || text.toLowerCase().contains("owner")) {
            strengths.add("Shows ownership or leadership signals.");
        }
        if (strengths.isEmpty()) {
            strengths.add("Resume content is concise and readable.");
        }
        return strengths;
    }

    private List<String> buildRisks(String text) {
        var risks = new ArrayList<String>();
        var lower = text.toLowerCase();
        if (!lower.contains("impact") && !lower.contains("result")) {
            risks.add("Project impact is not quantified clearly.");
        }
        if (!lower.contains("redis") && !lower.contains("mysql") && !lower.contains("database")) {
            risks.add("Infrastructure depth may need stronger evidence.");
        }
        if (text.length() < 200) {
            risks.add("Resume content is short. More detail could improve evaluation quality.");
        }
        if (risks.isEmpty()) {
            risks.add("No critical structural risk found. Validate with interview follow-up.");
        }
        return risks;
    }

    private String buildSummary(String candidateName, String position, List<String> keywords, List<String> strengths, List<String> risks) {
        return "Candidate " + candidateName
                + " appears aligned with " + position
                + ". Focus keywords: " + String.join(", ", keywords)
                + ". Main strengths: " + String.join(" ", strengths)
                + ". Watch-outs: " + String.join(" ", risks);
    }

    private String preview(String text) {
        var normalized = text.replaceAll("\\s+", " ").trim();
        if (normalized.length() <= PREVIEW_LIMIT) {
            return normalized;
        }
        return normalized.substring(0, PREVIEW_LIMIT) + "...";
    }
}
