package interview.backend.modules.resume;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import interview.backend.common.exception.BizException;
import interview.backend.infrastructure.ai.AiService;
import interview.backend.infrastructure.export.PdfExportService;
import interview.backend.infrastructure.file.DocumentTextExtractor;
import interview.backend.infrastructure.redis.RedisStreamService;
import interview.backend.infrastructure.storage.LocalStorageService;
import interview.backend.modules.resume.dto.ResumeUploadResponse;
import interview.backend.modules.resume.model.Resume;
import interview.backend.modules.resume.model.ResumeStatus;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ResumeService {

    private static final int PREVIEW_LIMIT = 240;

    private final ResumeMapper resumeMapper;
    private final LocalStorageService storageService;
    private final DocumentTextExtractor textExtractor;
    private final PdfExportService pdfExportService;
    private final RedisStreamService redisStreamService;
    private final AiService aiService;
    private final int maxRetry;

    public ResumeService(
            ResumeMapper resumeMapper,
            LocalStorageService storageService,
            DocumentTextExtractor textExtractor,
            PdfExportService pdfExportService,
            RedisStreamService redisStreamService,
            AiService aiService,
            @Value("${app.resume.max-retry:3}") int maxRetry
    ) {
        this.resumeMapper = resumeMapper;
        this.storageService = storageService;
        this.textExtractor = textExtractor;
        this.pdfExportService = pdfExportService;
        this.redisStreamService = redisStreamService;
        this.aiService = aiService;
        this.maxRetry = maxRetry;
    }

    public ResumeUploadResponse upload(MultipartFile file) {
        validateFile(file);
        try {
            var bytes = file.getBytes();
            var fileName = sanitizeFileName(file.getOriginalFilename());
            var hash = sha256(bytes);

            var existing = resumeMapper.selectOne(
                    new QueryWrapper<Resume>().eq("content_hash", hash)
            );
            if (existing != null) {
                return new ResumeUploadResponse(existing.getId(), existing.getStatus(), true);
            }

            var filePath = storageService.save(file, "resume");
            var resume = Resume.create(fileName, filePath, hash);
            resumeMapper.insert(resume);
            redisStreamService.publish("resume-analysis", String.valueOf(resume.getId()), "queued");
            startAnalysis(resume.getId(), bytes, fileName);
            return new ResumeUploadResponse(resume.getId(), ResumeStatus.PENDING, false);
        } catch (IOException ex) {
            throw new BizException("Resume upload failed: " + ex.getMessage());
        }
    }

    public List<Resume> list() {
        return resumeMapper.selectList(
                new QueryWrapper<Resume>().orderByDesc("updated_at")
        );
    }

    public Resume detail(Long id) {
        var resume = resumeMapper.selectById(id);
        if (resume == null) {
            throw new BizException("Resume record not found");
        }
        return resume;
    }

    public Resume retry(Long id) {
        var resume = detail(id);
        if (resume.getRetryCount() >= maxRetry) {
            throw new BizException("Retry limit reached");
        }
        try {
            var fileBytes = Files.readAllBytes(Path.of(resume.getFilePath()));
            resume.setStatus(ResumeStatus.PENDING);
            resume.setRetryCount(resume.getRetryCount() + 1);
            resume.setProgress(5);
            resume.setAnalysisSummary("Requeued for analysis.");
            resume.setUpdatedAt(LocalDateTime.now());
            resumeMapper.updateById(resume);
            redisStreamService.publish("resume-analysis", String.valueOf(id), "retry");
            startAnalysis(id, fileBytes, resume.getFileName());
            return resume;
        } catch (IOException ex) {
            throw new BizException("Retry failed: " + ex.getMessage());
        }
    }

    public Map<String, Object> stats() {
        var pending = resumeMapper.selectCount(
                new QueryWrapper<Resume>().eq("status", ResumeStatus.PENDING)
        );
        var analyzing = resumeMapper.selectCount(
                new QueryWrapper<Resume>().eq("status", ResumeStatus.ANALYZING)
        );
        var completed = resumeMapper.selectCount(
                new QueryWrapper<Resume>().eq("status", ResumeStatus.COMPLETED)
        );
        var failed = resumeMapper.selectCount(
                new QueryWrapper<Resume>().eq("status", ResumeStatus.FAILED)
        );
        return Map.of(
                "total", resumeMapper.selectCount(null),
                "pending", pending,
                "analyzing", analyzing,
                "completed", completed,
                "failed", failed
        );
    }

    public byte[] exportReport(Long id) {
        var resume = detail(id);
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
                resume.getCandidateName(),
                resume.getTargetPosition(),
                resume.getFileName(),
                resume.getStatus(),
                resume.getRetryCount(),
                resume.getAnalysisSummary(),
                String.join(", ", resume.getKeywords() != null ? resume.getKeywords() : List.of()),
                String.join("; ", resume.getStrengths() != null ? resume.getStrengths() : List.of()),
                String.join("; ", resume.getRisks() != null ? resume.getRisks() : List.of()),
                resume.getRawTextPreview()
        );
        return pdfExportService.exportTextReport("Resume Analysis Report", body);
    }

    private void startAnalysis(Long id, byte[] fileBytes, String fileName) {
        Thread.startVirtualThread(() -> analyze(id, fileBytes, fileName));
    }

    private void analyze(Long id, byte[] fileBytes, String fileName) {
        Resume resume = null;
        try {
            resume = detail(id);
            updateStatus(resume, ResumeStatus.ANALYZING, 20, "Parsing resume content...");
            redisStreamService.publish("resume-analysis", String.valueOf(id), "analyzing");

            var text = textExtractor.extract(new ByteArrayInputStream(fileBytes), fileName);
            if (text.isBlank()) {
                throw new BizException("No readable text found in the uploaded document");
            }

            updateStatus(resume, ResumeStatus.ANALYZING, 55, "Extracting profile and skill signals...");

            var aiResponse = aiService.chat("You are a resume analyst. Extract candidate info in JSON format.", text);
            var analysisResult = parseAiResponse(aiResponse, text);

            var current = detail(id);
            current.setStatus(ResumeStatus.COMPLETED);
            current.setProgress(100);
            current.setCandidateName(analysisResult.candidateName);
            current.setTargetPosition(analysisResult.targetPosition);
            current.setAnalysisSummary(analysisResult.summary);
            current.setKeywords(analysisResult.keywords);
            current.setStrengths(analysisResult.strengths);
            current.setRisks(analysisResult.risks);
            current.setRawTextPreview(preview(text));
            current.setUpdatedAt(LocalDateTime.now());
            resumeMapper.updateById(current);
            redisStreamService.publish("resume-analysis", String.valueOf(id), "completed");
        } catch (Exception ex) {
            var current = detail(id);
            current.setStatus(ResumeStatus.FAILED);
            current.setAnalysisSummary("Analysis failed: " + ex.getMessage());
            current.setUpdatedAt(LocalDateTime.now());
            resumeMapper.updateById(current);
            redisStreamService.publish("resume-analysis", String.valueOf(id), "failed");
        }
    }

    private void updateStatus(Resume resume, ResumeStatus status, int progress, String summary) {
        resume.setStatus(status);
        resume.setProgress(progress);
        resume.setAnalysisSummary(summary);
        resume.setUpdatedAt(LocalDateTime.now());
        resumeMapper.updateById(resume);
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

    private AnalysisResult parseAiResponse(String aiResponse, String originalText) {
        try {
            var json = aiResponse;
            if (aiResponse.contains("{") && aiResponse.contains("}")) {
                int start = aiResponse.indexOf("{");
                int end = aiResponse.lastIndexOf("}") + 1;
                json = aiResponse.substring(start, end);
            }
            var parsed = JSON.parseObject(json);
            return new AnalysisResult(
                    parsed.getString("candidateName"),
                    parsed.getString("targetPosition"),
                    parsed.getString("summary"),
                    parsed.getList("keywords", String.class),
                    parsed.getList("strengths", String.class),
                    parsed.getList("risks", String.class)
            );
        } catch (Exception ex) {
            return new AnalysisResult(
                    detectName(originalText),
                    detectPosition(originalText),
                    "Analysis completed with fallback parsing.",
                    List.of("communication"),
                    List.of("Resume content is readable."),
                    List.of("Manual review recommended.")
            );
        }
    }

    private String detectName(String text) {
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
        return "Unknown Candidate";
    }

    private String detectPosition(String text) {
        var lower = text.toLowerCase();
        if (lower.contains("java")) return "Java Backend Engineer";
        if (lower.contains("frontend") || lower.contains("vue") || lower.contains("react")) return "Frontend Engineer";
        if (lower.contains("python")) return "Python Engineer";
        if (lower.contains("algorithm")) return "Algorithm Engineer";
        if (lower.contains("ai")) return "AI Engineer";
        return "General Software Engineer";
    }

    private String preview(String text) {
        var normalized = text == null ? "" : text.replaceAll("\\s+", " ").trim();
        if (normalized.length() <= PREVIEW_LIMIT) {
            return normalized;
        }
        return normalized.substring(0, PREVIEW_LIMIT) + "...";
    }

    private record AnalysisResult(
            String candidateName,
            String targetPosition,
            String summary,
            List<String> keywords,
            List<String> strengths,
            List<String> risks
    ) {}
}
