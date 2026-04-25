# Database Persistence & AI Interface Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace in-memory ConcurrentHashMap storage with MySQL + MyBatis-Plus persistence for all modules, and add AiService interface for future AI integration.

**Architecture:** Each module gets Flyway migration SQL, Entity class (`@Data` + `@TableName`), Mapper interface (`BaseMapper<T>`), and Service refactored to use Mapper + AiService. Controllers and DTOs remain unchanged.

**Tech Stack:** Spring Boot 3.5.2, Java 21, MyBatis-Plus 3.5.10.1, MySQL 8, Flyway, Lombok

---

## Task 1: AI Service Interface Layer

**Files:**
- Create: `interview-backend/app/src/main/java/interview/backend/infrastructure/ai/AiService.java`
- Create: `interview-backend/app/src/main/java/interview/backend/infrastructure/ai/StubAiService.java`
- Create: `interview-backend/app/src/main/java/interview/backend/infrastructure/ai/AiProperties.java`

- [ ] **Step 1: Create AiService interface**

```java
// interview-backend/app/src/main/java/interview/backend/infrastructure/ai/AiService.java
package interview.backend.infrastructure.ai;

import reactor.core.publisher.Flux;

public interface AiService {
    /**
     * Single-turn chat, returns AI text response.
     */
    String chat(String systemPrompt, String userMessage);

    /**
     * Streaming chat, returns SSE event stream.
     */
    Flux<String> chatStream(String systemPrompt, String userMessage);
}
```

- [ ] **Step 2: Create AiProperties for configuration**

```java
// interview-backend/app/src/main/java/interview/backend/infrastructure/ai/AiProperties.java
package interview.backend.infrastructure.ai;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.ai")
public record AiProperties(
        boolean enabled,
        String provider,
        String apiKey,
        String model,
        String baseUrl
) {
    public AiProperties() {
        this(false, "stub", "", "", "");
    }
}
```

- [ ] **Step 3: Create StubAiService implementation**

```java
// interview-backend/app/src/main/java/interview/backend/infrastructure/ai/StubAiService.java
package interview.backend.infrastructure.ai;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@ConditionalOnProperty(name = "app.ai.enabled", havingValue = "false", matchIfMissing = true)
public class StubAiService implements AiService {

    @Override
    public String chat(String systemPrompt, String userMessage) {
        // Resume analysis stub
        if (systemPrompt != null && systemPrompt.toLowerCase().contains("resume")) {
            return analyzeResumeStub(userMessage);
        }
        // Interview evaluation stub
        if (systemPrompt != null && systemPrompt.toLowerCase().contains("interview")) {
            return evaluateInterviewStub(userMessage);
        }
        // Schedule extraction stub
        if (systemPrompt != null && systemPrompt.toLowerCase().contains("schedule")) {
            return extractScheduleStub(userMessage);
        }
        // Knowledge QA stub
        if (systemPrompt != null && systemPrompt.toLowerCase().contains("knowledge")) {
            return answerKnowledgeStub(userMessage);
        }
        // Voice interview stub
        if (systemPrompt != null && systemPrompt.toLowerCase().contains("voice")) {
            return voiceReplyStub(userMessage);
        }
        return "AI response placeholder for: " + truncate(userMessage, 100);
    }

    @Override
    public Flux<String> chatStream(String systemPrompt, String userMessage) {
        return Flux.just("Streaming not implemented in stub mode. Response: ", chat(systemPrompt, userMessage));
    }

    private String analyzeResumeStub(String resumeText) {
        var name = detectName(resumeText);
        var position = detectPosition(resumeText);
        return String.format("""
                {"candidateName":"%s","targetPosition":"%s","keywords":["java","spring","mysql"],"strengths":["Relevant technical stack coverage"],"risks":["Project impact not quantified"],"summary":"Candidate %s appears aligned with %s."}
                """, name, position, name, position);
    }

    private String evaluateInterviewStub(String transcript) {
        var score = Math.min(85, 50 + transcript.length() / 10);
        return String.format("""
                {"score":%d,"strengths":["Response structure is complete"],"risks":["Need deeper project details"],"feedback":"Overall performance is satisfactory with areas for improvement."}
                """, score);
    }

    private String extractScheduleStub(String content) {
        return String.format("""
                {"company":"Unknown Company","position":"Backend Engineer","meetingLink":"https://meeting.example.com","startTime":"%s"}
                """, java.time.LocalDateTime.now().plusDays(1).toString());
    }

    private String answerKnowledgeStub(String question) {
        return "Based on the knowledge base: " + truncate(question, 50) + " - This is a placeholder AI response.";
    }

    private String voiceReplyStub(String transcript) {
        if (transcript.toLowerCase().contains("redis") || transcript.toLowerCase().contains("cache")) {
            return "Good. Why was Redis a better choice than local cache or database reads in that scenario?";
        }
        if (transcript.toLowerCase().contains("project")) {
            return "Please quantify the project impact with latency, throughput, stability, or delivery metrics.";
        }
        return "Please drill deeper into architecture, trade-offs, and measurable business impact.";
    }

    private String detectName(String text) {
        var lines = text.lines().map(String::trim).filter(l -> !l.isBlank()).limit(8).toList();
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
        return "Backend Engineer";
    }

    private String truncate(String text, int max) {
        if (text == null) return "";
        return text.length() <= max ? text : text.substring(0, max) + "...";
    }
}
```

- [ ] **Step 4: Register AiProperties in application config**

Edit `interview-backend/app/src/main/java/interview/backend/App.java` to add `@EnableConfigurationProperties`:

```java
package interview.backend;

import interview.backend.infrastructure.ai.AiProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(AiProperties.class)
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}
```

- [ ] **Step 5: Add AI config to application.yml**

Edit `interview-backend/app/src/main/resources/application.yml`, append:

```yaml
app:
  ai:
    enabled: false
    provider: stub
    api-key: ""
    model: ""
    base-url: ""
```

- [ ] **Step 6: Commit AI interface layer**

```bash
git add interview-backend/app/src/main/java/interview/backend/infrastructure/ai/
git add interview-backend/app/src/main/java/interview/backend/App.java
git add interview-backend/app/src/main/resources/application.yml
git commit -m "feat: add AiService interface with StubAiService implementation

Co-Authored-By: Claude Opus 4.7 <noreply@anthropic.com>"
```

---

## Task 2: Resume Module - Database Migration

**Files:**
- Create: `interview-backend/app/src/main/resources/db/migration/V2__Create_resume_table.sql`

- [ ] **Step 1: Create Flyway migration for resume table**

```sql
-- interview-backend/app/src/main/resources/db/migration/V2__Create_resume_table.sql
CREATE TABLE IF NOT EXISTS resume (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    content_hash VARCHAR(64) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    retry_count INT DEFAULT 0,
    progress INT DEFAULT 0,
    duplicated TINYINT(1) DEFAULT 0,
    candidate_name VARCHAR(100),
    target_position VARCHAR(100),
    analysis_summary TEXT,
    keywords JSON,
    strengths JSON,
    risks JSON,
    raw_text_preview TEXT,
    user_id BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_status (status),
    INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

- [ ] **Step 2: Commit migration**

```bash
git add interview-backend/app/src/main/resources/db/migration/V2__Create_resume_table.sql
git commit -m "feat(resume): add Flyway migration for resume table

Co-Authored-By: Claude Opus 4.7 <noreply@anthropic.com>"
```

---

## Task 3: Resume Module - Entity and Mapper

**Files:**
- Create: `interview-backend/app/src/main/java/interview/backend/modules/resume/model/Resume.java`
- Create: `interview-backend/app/src/main/java/interview/backend/modules/resume/ResumeMapper.java`

- [ ] **Step 1: Create Resume entity class**

```java
// interview-backend/app/src/main/java/interview/backend/modules/resume/model/Resume.java
package interview.backend.modules.resume.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("resume")
public class Resume {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String fileName;
    private String filePath;
    private String contentHash;
    private ResumeStatus status;
    private Integer retryCount;
    private Integer progress;
    private Boolean duplicated;
    private String candidateName;
    private String targetPosition;
    private String analysisSummary;
    private List<String> keywords;
    private List<String> strengths;
    private List<String> risks;
    private String rawTextPreview;
    private Long userId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static Resume create(String fileName, String filePath, String contentHash) {
        Resume resume = new Resume();
        resume.setFileName(fileName);
        resume.setFilePath(filePath);
        resume.setContentHash(contentHash);
        resume.setStatus(ResumeStatus.PENDING);
        resume.setRetryCount(0);
        resume.setProgress(0);
        resume.setDuplicated(false);
        resume.setCandidateName("Unknown Candidate");
        resume.setTargetPosition("General Software Engineer");
        resume.setAnalysisSummary("Queued for resume analysis.");
        resume.setCreatedAt(LocalDateTime.now());
        resume.setUpdatedAt(LocalDateTime.now());
        return resume;
    }
}
```

- [ ] **Step 2: Create ResumeMapper interface**

```java
// interview-backend/app/src/main/java/interview/backend/modules/resume/ResumeMapper.java
package interview.backend.modules.resume;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import interview.backend.modules.resume.model.Resume;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ResumeMapper extends BaseMapper<Resume> {
}
```

- [ ] **Step 3: Add TypeHandler for List<String> JSON fields**

Create `interview-backend/app/src/main/java/interview/backend/common/handler/JsonListTypeHandler.java`:

```java
// interview-backend/app/src/main/java/interview/backend/common/handler/JsonListTypeHandler.java
package interview.backend.common.handler;

import com.alibaba.fastjson2.JSON;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

@MappedTypes(List.class)
public class JsonListTypeHandler extends BaseTypeHandler<List<String>> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<String> parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, JSON.toJSONString(parameter));
    }

    @Override
    public List<String> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        return parseList(value);
    }

    @Override
    public List<String> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String value = rs.getString(columnIndex);
        return parseList(value);
    }

    @Override
    public List<String> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String value = cs.getString(columnIndex);
        return parseList(value);
    }

    private List<String> parseList(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return JSON.parseArray(value, String.class);
    }
}
```

- [ ] **Step 4: Commit entity and mapper**

```bash
git add interview-backend/app/src/main/java/interview/backend/modules/resume/model/Resume.java
git add interview-backend/app/src/main/java/interview/backend/modules/resume/ResumeMapper.java
git add interview-backend/app/src/main/java/interview/backend/common/handler/JsonListTypeHandler.java
git commit -m "feat(resume): add Resume entity and ResumeMapper with JSON TypeHandler

Co-Authored-By: Claude Opus 4.7 <noreply@anthropic.com>"
```

---

## Task 4: Resume Module - Service Refactoring

**Files:**
- Modify: `interview-backend/app/src/main/java/interview/backend/modules/resume/ResumeService.java`

- [ ] **Step 1: Refactor ResumeService to use ResumeMapper and AiService**

Replace the entire `ResumeService.java`. Key changes:
- Remove `ConcurrentHashMap` storage and `AtomicLong idGenerator`
- Inject `ResumeMapper` and `AiService`
- Use `ResumeMapper.selectById`, `.insert`, `.updateById` instead of map operations
- Use `AiService.chat()` for resume analysis (currently stub)

```java
// interview-backend/app/src/main/java/interview/backend/modules/resume/ResumeService.java
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

            // Use AI service for analysis
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
```

- [ ] **Step 2: Commit ResumeService refactoring**

```bash
git add interview-backend/app/src/main/java/interview/backend/modules/resume/ResumeService.java
git commit -m "refactor(resume): use ResumeMapper and AiService instead of ConcurrentHashMap

Co-Authored-By: Claude Opus 4.7 <noreply@anthropic.com>"
```

---

## Task 5: Resume Module - Controller Update

**Files:**
- Modify: `interview-backend/app/src/main/java/interview/backend/modules/resume/ResumeController.java`

- [ ] **Step 1: Update ResumeController to return Resume entity**

The controller should already work with the service changes, but verify the return type matches:

```java
// interview-backend/app/src/main/java/interview/backend/modules/resume/ResumeController.java
package interview.backend.modules.resume;

import interview.backend.common.result.ApiResponse;
import interview.backend.modules.resume.dto.ResumeUploadResponse;
import interview.backend.modules.resume.model.Resume;
import java.util.List;
import java.util.Map;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/resumes")
public class ResumeController {

    private final ResumeService resumeService;

    public ResumeController(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    @PostMapping
    public ApiResponse<ResumeUploadResponse> upload(@RequestParam("file") MultipartFile file) {
        return ApiResponse.success("Resume uploaded", resumeService.upload(file));
    }

    @GetMapping
    public ApiResponse<List<Resume>> list() {
        return ApiResponse.success(resumeService.list());
    }

    @GetMapping("/stats")
    public ApiResponse<Map<String, Object>> stats() {
        return ApiResponse.success(resumeService.stats());
    }

    @GetMapping("/{id}")
    public ApiResponse<Resume> detail(@PathVariable Long id) {
        return ApiResponse.success(resumeService.detail(id));
    }

    @PostMapping("/{id}/retry")
    public ApiResponse<Resume> retry(@PathVariable Long id) {
        return ApiResponse.success("Retry submitted", resumeService.retry(id));
    }

    @GetMapping("/{id}/report")
    public ResponseEntity<byte[]> exportReport(@PathVariable Long id) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                        ContentDisposition.attachment().filename("resume-report-" + id + ".pdf").build().toString())
                .contentType(MediaType.APPLICATION_PDF)
                .body(resumeService.exportReport(id));
    }
}
```

- [ ] **Step 2: Commit controller update**

```bash
git add interview-backend/app/src/main/java/interview/backend/modules/resume/ResumeController.java
git commit -m "refactor(resume): update controller to use Resume entity

Co-Authored-By: Claude Opus 4.7 <noreply@anthropic.com>"
```

---

## Task 6: Interview Module - Database Migration

**Files:**
- Create: `interview-backend/app/src/main/resources/db/migration/V3__Create_interview_tables.sql`

- [ ] **Step 1: Create Flyway migration for interview tables**

```sql
-- interview-backend/app/src/main/resources/db/migration/V3__Create_interview_tables.sql
CREATE TABLE IF NOT EXISTS interview_session (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    direction VARCHAR(50) NOT NULL,
    total_minutes INT NOT NULL,
    stage_durations JSON,
    follow_up_rounds INT DEFAULT 0,
    current_question_id VARCHAR(36),
    transcript TEXT,
    evaluation TEXT,
    status VARCHAR(30) NOT NULL DEFAULT 'IN_PROGRESS',
    user_id BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_status (status),
    INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS interview_question (
    id VARCHAR(36) PRIMARY KEY,
    session_id BIGINT NOT NULL,
    stage VARCHAR(30) NOT NULL,
    content TEXT NOT NULL,
    sort_order INT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_session (session_id),
    FOREIGN KEY (session_id) REFERENCES interview_session(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

- [ ] **Step 2: Commit migration**

```bash
git add interview-backend/app/src/main/resources/db/migration/V3__Create_interview_tables.sql
git commit -m "feat(interview): add Flyway migration for interview_session and interview_question tables

Co-Authored-By: Claude Opus 4.7 <noreply@anthropic.com>"
```

---

## Task 7: Interview Module - Entity and Mapper

**Files:**
- Create: `interview-backend/app/src/main/java/interview/backend/modules/interview/model/InterviewSessionEntity.java`
- Create: `interview-backend/app/src/main/java/interview/backend/modules/interview/model/InterviewQuestionEntity.java`
- Create: `interview-backend/app/src/main/java/interview/backend/modules/interview/InterviewSessionMapper.java`
- Create: `interview-backend/app/src/main/java/interview/backend/modules/interview/InterviewQuestionMapper.java`
- Create: `interview-backend/app/src/main/java/interview/backend/common/handler/JsonMapTypeHandler.java`

- [ ] **Step 1: Create InterviewSessionEntity**

```java
// interview-backend/app/src/main/java/interview/backend/modules/interview/model/InterviewSessionEntity.java
package interview.backend.modules.interview.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import interview.backend.common.handler.JsonMapTypeHandler;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("interview_session")
public class InterviewSessionEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String direction;
    private Integer totalMinutes;
    @TableField(typeHandler = JsonMapTypeHandler.class)
    private Map<String, Integer> stageDurations;
    private Integer followUpRounds;
    private String currentQuestionId;
    private String transcript;
    private String evaluation;
    private String status;
    private Long userId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

- [ ] **Step 2: Create InterviewQuestionEntity**

```java
// interview-backend/app/src/main/java/interview/backend/modules/interview/model/InterviewQuestionEntity.java
package interview.backend.modules.interview.model;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("interview_question")
public class InterviewQuestionEntity {
    @TableId
    private String id;
    private Long sessionId;
    private String stage;
    private String content;
    private Integer sortOrder;
    private LocalDateTime createdAt;

    public static InterviewQuestionEntity create(String id, Long sessionId, String stage, String content, int sortOrder) {
        var entity = new InterviewQuestionEntity();
        entity.setId(id);
        entity.setSessionId(sessionId);
        entity.setStage(stage);
        entity.setContent(content);
        entity.setSortOrder(sortOrder);
        entity.setCreatedAt(LocalDateTime.now());
        return entity;
    }
}
```

- [ ] **Step 3: Create JsonMapTypeHandler**

```java
// interview-backend/app/src/main/java/interview/backend/common/handler/JsonMapTypeHandler.java
package interview.backend.common.handler;

import com.alibaba.fastjson2.JSON;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

@MappedTypes(Map.class)
public class JsonMapTypeHandler extends BaseTypeHandler<Map<String, Integer>> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Map<String, Integer> parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, JSON.toJSONString(parameter));
    }

    @Override
    public Map<String, Integer> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        return parseMap(value);
    }

    @Override
    public Map<String, Integer> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String value = rs.getString(columnIndex);
        return parseMap(value);
    }

    @Override
    public Map<String, Integer> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String value = cs.getString(columnIndex);
        return parseMap(value);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Integer> parseMap(String value) {
        if (value == null || value.isBlank()) {
            return Map.of();
        }
        return JSON.parseObject(value, Map.class);
    }
}
```

- [ ] **Step 4: Create InterviewSessionMapper**

```java
// interview-backend/app/src/main/java/interview/backend/modules/interview/InterviewSessionMapper.java
package interview.backend.modules.interview;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import interview.backend.modules.interview.model.InterviewSessionEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface InterviewSessionMapper extends BaseMapper<InterviewSessionEntity> {
}
```

- [ ] **Step 5: Create InterviewQuestionMapper**

```java
// interview-backend/app/src/main/java/interview/backend/modules/interview/InterviewQuestionMapper.java
package interview.backend.modules.interview;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import interview.backend.modules.interview.model.InterviewQuestionEntity;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface InterviewQuestionMapper extends BaseMapper<InterviewQuestionEntity> {

    @Select("SELECT * FROM interview_question WHERE session_id = #{sessionId} ORDER BY sort_order")
    List<InterviewQuestionEntity> findBySessionIdOrderBySortOrder(Long sessionId);
}
```

- [ ] **Step 6: Commit entity and mapper**

```bash
git add interview-backend/app/src/main/java/interview/backend/modules/interview/model/InterviewSessionEntity.java
git add interview-backend/app/src/main/java/interview/backend/modules/interview/model/InterviewQuestionEntity.java
git add interview-backend/app/src/main/java/interview/backend/modules/interview/InterviewSessionMapper.java
git add interview-backend/app/src/main/java/interview/backend/modules/interview/InterviewQuestionMapper.java
git add interview-backend/app/src/main/java/interview/backend/common/handler/JsonMapTypeHandler.java
git commit -m "feat(interview): add InterviewSessionEntity, InterviewQuestionEntity and Mappers

Co-Authored-By: Claude Opus 4.7 <noreply@anthropic.com>"
```

---

## Task 8: Interview Module - Service Refactoring

**Files:**
- Modify: `interview-backend/app/src/main/java/interview/backend/modules/interview/InterviewService.java`

- [ ] **Step 1: Refactor InterviewService to use Mappers and AiService**

Replace the entire `InterviewService.java`:

```java
// interview-backend/app/src/main/java/interview/backend/modules/interview/InterviewService.java
package interview.backend.modules.interview;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import interview.backend.common.exception.BizException;
import interview.backend.infrastructure.ai.AiService;
import interview.backend.infrastructure.export.PdfExportService;
import interview.backend.modules.interview.dto.CreateInterviewSessionRequest;
import interview.backend.modules.interview.model.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InterviewService {

    private final InterviewSessionMapper sessionMapper;
    private final InterviewQuestionMapper questionMapper;
    private final SkillService skillService;
    private final PdfExportService pdfExportService;
    private final AiService aiService;

    public InterviewService(
            InterviewSessionMapper sessionMapper,
            InterviewQuestionMapper questionMapper,
            SkillService skillService,
            PdfExportService pdfExportService,
            AiService aiService
    ) {
        this.sessionMapper = sessionMapper;
        this.questionMapper = questionMapper;
        this.skillService = skillService;
        this.pdfExportService = pdfExportService;
        this.aiService = aiService;
    }

    @Transactional
    public InterviewSession create(CreateInterviewSessionRequest request) {
        var direction = InterviewDirection.valueOf(request.direction());
        var durations = distribute(request.totalMinutes());
        var entity = new InterviewSessionEntity();
        entity.setDirection(direction.name());
        entity.setTotalMinutes(request.totalMinutes());
        entity.setStageDurations(durations);
        entity.setFollowUpRounds(request.followUpRounds());
        entity.setStatus("IN_PROGRESS");
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        sessionMapper.insert(entity);

        var questions = buildQuestions(direction, request.followUpRounds());
        for (int i = 0; i < questions.size(); i++) {
            var q = questions.get(i);
            var qEntity = InterviewQuestionEntity.create(q.id(), entity.getId(), q.stage().name(), q.content(), i);
            questionMapper.insert(qEntity);
        }

        entity.setCurrentQuestionId(questions.isEmpty() ? null : questions.getFirst().id());
        sessionMapper.updateById(entity);
        return toSession(entity, questions);
    }

    public List<InterviewSession> list() {
        return sessionMapper.selectList(
                new QueryWrapper<InterviewSessionEntity>().orderByDesc("updated_at")
        ).stream()
                .map(e -> toSession(e, loadQuestions(e.getId())))
                .toList();
    }

    public InterviewSession continueSession(Long id) {
        var entity = sessionMapper.selectById(id);
        if (entity == null) {
            throw new BizException("Interview session not found");
        }
        return toSession(entity, loadQuestions(id));
    }

    @Transactional
    public InterviewSession answer(Long id, String answer) {
        var entity = sessionMapper.selectById(id);
        if (entity == null) {
            throw new BizException("Interview session not found");
        }
        if (!"IN_PROGRESS".equals(entity.getStatus())) {
            throw new BizException("Interview session is not accepting new answers");
        }

        var questions = loadQuestions(id);
        var currentQuestion = questions.stream()
                .filter(q -> q.id().equals(entity.getCurrentQuestionId()))
                .findFirst()
                .orElseThrow(() -> new BizException("Current question not found"));

        var updatedTranscript = (entity.getTranscript() == null ? "" : entity.getTranscript())
                + "\nQ: " + currentQuestion.content()
                + "\nA: " + answer.trim()
                + "\n";

        var nextQuestion = findNextQuestion(questions, entity.getCurrentQuestionId());

        entity.setTranscript(updatedTranscript.trim());
        entity.setCurrentQuestionId(nextQuestion == null ? null : nextQuestion.id());
        entity.setStatus(nextQuestion == null ? "READY_FOR_EVALUATION" : "IN_PROGRESS");
        entity.setUpdatedAt(LocalDateTime.now());
        sessionMapper.updateById(entity);

        return toSession(entity, questions);
    }

    @Transactional
    public InterviewSession evaluate(Long id, String transcript) {
        var entity = sessionMapper.selectById(id);
        if (entity == null) {
            throw new BizException("Interview session not found");
        }
        var effectiveTranscript = transcript == null || transcript.isBlank() ? entity.getTranscript() : transcript;

        // Use AI service for evaluation
        var direction = InterviewDirection.valueOf(entity.getDirection());
        var skillDefinition = skillService.loadDefinition(direction);
        var aiResponse = aiService.chat(
                "You are an interviewer evaluating candidate responses. Skill reference: " + skillDefinition,
                "Evaluate this interview transcript:\n" + effectiveTranscript
        );

        var evaluation = parseEvaluation(aiResponse, entity, effectiveTranscript);

        entity.setTranscript(effectiveTranscript);
        entity.setEvaluation(evaluation);
        entity.setStatus("COMPLETED");
        entity.setUpdatedAt(LocalDateTime.now());
        sessionMapper.updateById(entity);

        return toSession(entity, loadQuestions(id));
    }

    public Map<String, Object> centerSummary() {
        var total = sessionMapper.selectCount(null);
        var completed = sessionMapper.selectCount(
                new QueryWrapper<InterviewSessionEntity>().eq("status", "COMPLETED")
        );
        var active = sessionMapper.selectCount(
                new QueryWrapper<InterviewSessionEntity>().eq("status", "IN_PROGRESS")
        );
        var ready = sessionMapper.selectCount(
                new QueryWrapper<InterviewSessionEntity>().eq("status", "READY_FOR_EVALUATION")
        );
        var directions = sessionMapper.selectList(null).stream()
                .collect(Collectors.groupingBy(InterviewSessionEntity::getDirection, Collectors.counting()));
        return Map.of(
                "totalSessions", total,
                "completedSessions", completed,
                "activeSessions", active,
                "readyForEvaluation", ready,
                "directions", directions
        );
    }

    public List<Map<String, String>> directions() {
        var result = new ArrayList<Map<String, String>>();
        for (var direction : InterviewDirection.values()) {
            result.add(Map.of(
                    "code", direction.name(),
                    "label", direction.name().replace('_', ' '),
                    "preview", preview(skillService.loadDefinition(direction))
            ));
        }
        return result;
    }

    public byte[] exportReport(Long id) {
        var session = continueSession(id);
        var body = """
                Direction: %s
                Total Minutes: %s
                Status: %s
                Follow-up Rounds: %s

                Stage Durations:
                %s

                Questions:
                %s

                Transcript:
                %s

                Evaluation:
                %s
                """.formatted(
                session.direction(),
                session.totalMinutes(),
                session.status(),
                session.followUpRounds(),
                session.stageDurations(),
                session.askedQuestions().stream().map(InterviewQuestion::content).toList(),
                session.transcript() == null || session.transcript().isBlank() ? "No transcript yet." : session.transcript(),
                session.evaluation() == null ? "No evaluation yet." : session.evaluation()
        );
        return pdfExportService.exportTextReport("Mock Interview Report", body);
    }

    private List<InterviewQuestion> loadQuestions(Long sessionId) {
        return questionMapper.findBySessionIdOrderBySortOrder(sessionId).stream()
                .map(e -> new InterviewQuestion(
                        e.getId(),
                        InterviewStage.valueOf(e.getStage()),
                        e.getContent()
                ))
                .toList();
    }

    private InterviewSession toSession(InterviewSessionEntity entity, List<InterviewQuestion> questions) {
        var stageDurations = entity.getStageDurations() != null
                ? entity.getStageDurations().entrySet().stream()
                .collect(Collectors.toMap(
                        e -> InterviewStage.valueOf(e.getKey()),
                        Map.Entry::getValue,
                        (a, b) -> a,
                        LinkedHashMap::new
                ))
                : Map.<InterviewStage, Integer>of();

        return new InterviewSession(
                entity.getId(),
                InterviewDirection.valueOf(entity.getDirection()),
                entity.getTotalMinutes(),
                stageDurations,
                entity.getFollowUpRounds(),
                questions,
                entity.getCurrentQuestionId(),
                entity.getTranscript() != null ? entity.getTranscript() : "",
                entity.getEvaluation(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private InterviewQuestion findNextQuestion(List<InterviewQuestion> questions, String currentId) {
        if (currentId == null) return null;
        for (int i = 0; i < questions.size(); i++) {
            if (questions.get(i).id().equals(currentId)) {
                return i + 1 >= questions.size() ? null : questions.get(i + 1);
            }
        }
        return null;
    }

    private Map<String, Integer> distribute(int totalMinutes) {
        var result = new LinkedHashMap<String, Integer>();
        result.put("INTRODUCTION", Math.max(3, totalMinutes * 15 / 100));
        result.put("TECHNICAL", Math.max(6, totalMinutes * 45 / 100));
        result.put("PROJECT", Math.max(4, totalMinutes * 25 / 100));
        var used = result.values().stream().mapToInt(Integer::intValue).sum();
        result.put("QA", Math.max(2, totalMinutes - used));
        return result;
    }

    private List<InterviewQuestion> buildQuestions(InterviewDirection direction, int followUpRounds) {
        var skillPreview = preview(skillService.loadDefinition(direction));
        var questions = new ArrayList<InterviewQuestion>();
        questions.add(question(InterviewStage.INTRODUCTION, "Please give a concise self-introduction focused on your recent experience."));
        questions.add(question(InterviewStage.TECHNICAL, "What are the most important skills for " + direction.name().replace('_', ' ') + " and how have you used them?"));
        questions.add(question(InterviewStage.TECHNICAL, "Describe a difficult technical issue you solved and explain the trade-offs you considered."));
        questions.add(question(InterviewStage.PROJECT, "Pick one representative project and walk through goal, architecture, constraints, and measurable impact."));
        questions.add(question(InterviewStage.QA, "If you could ask the interviewer only two questions, what would you ask and why?"));
        for (int i = 1; i <= followUpRounds; i++) {
            questions.add(question(InterviewStage.TECHNICAL, "Follow-up " + i + ": based on the previous answer, what would you improve if you had one more sprint?"));
        }
        if (!skillPreview.isBlank()) {
            questions.add(question(InterviewStage.TECHNICAL, "Skill file focus reminder: " + skillPreview));
        }
        return questions;
    }

    private InterviewQuestion question(InterviewStage stage, String content) {
        return new InterviewQuestion(UUID.randomUUID().toString(), stage, content);
    }

    private String parseEvaluation(String aiResponse, InterviewSessionEntity entity, String transcript) {
        if (aiResponse != null && !aiResponse.isBlank()) {
            return aiResponse;
        }
        var score = Math.min(65, transcript.length() / 10) + 10;
        var tradeOff = transcript.toLowerCase().contains("trade-off") ? 10 : 0;
        var project = transcript.toLowerCase().contains("project") ? 10 : 0;
        return """
                Overall Score: %d/100
                Direction: %s
                Follow-up Rounds: %s

                Strengths:
                - Response structure is %s
                - Relevant to direction: %s

                Risks:
                - %s
                """.formatted(
                Math.min(98, score + tradeOff + project),
                entity.getDirection(),
                entity.getFollowUpRounds(),
                transcript.length() > 300 ? "complete" : "still somewhat short",
                entity.getDirection(),
                transcript.length() < 240 ? "Need deeper project details and more quantifiable impact." : "No major weakness detected in the text sample."
        );
    }

    private String preview(String text) {
        var normalized = text.replaceAll("\\s+", " ").trim();
        if (normalized.length() <= 180) {
            return normalized;
        }
        return normalized.substring(0, 180) + "...";
    }
}
```

- [ ] **Step 2: Commit InterviewService refactoring**

```bash
git add interview-backend/app/src/main/java/interview/backend/modules/interview/InterviewService.java
git commit -m "refactor(interview): use InterviewSessionMapper and AiService instead of ConcurrentHashMap

Co-Authored-By: Claude Opus 4.7 <noreply@anthropic.com>"
```

---

## Task 9: Schedule Module - Database Migration

**Files:**
- Create: `interview-backend/app/src/main/resources/db/migration/V4__Create_interview_schedule_table.sql`

- [ ] **Step 1: Create Flyway migration for schedule table**

```sql
-- interview-backend/app/src/main/resources/db/migration/V4__Create_interview_schedule_table.sql
CREATE TABLE IF NOT EXISTS interview_schedule (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    company VARCHAR(100),
    position VARCHAR(100),
    start_time DATETIME NOT NULL,
    meeting_link VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    source VARCHAR(50),
    raw_content TEXT,
    user_id BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_status (status),
    INDEX idx_start_time (start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

- [ ] **Step 2: Commit migration**

```bash
git add interview-backend/app/src/main/resources/db/migration/V4__Create_interview_schedule_table.sql
git commit -m "feat(schedule): add Flyway migration for interview_schedule table

Co-Authored-By: Claude Opus 4.7 <noreply@anthropic.com>"
```

---

## Task 10: Schedule Module - Entity, Mapper, Service

**Files:**
- Create: `interview-backend/app/src/main/java/interview/backend/modules/interviewschedule/model/ScheduleEntity.java`
- Create: `interview-backend/app/src/main/java/interview/backend/modules/interviewschedule/ScheduleMapper.java`
- Modify: `interview-backend/app/src/main/java/interview/backend/modules/interviewschedule/InterviewScheduleService.java`

- [ ] **Step 1: Create ScheduleEntity**

```java
// interview-backend/app/src/main/java/interview/backend/modules/interviewschedule/model/ScheduleEntity.java
package interview.backend.modules.interviewschedule.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("interview_schedule")
public class ScheduleEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String company;
    private String position;
    private LocalDateTime startTime;
    private String meetingLink;
    private String status;
    private String source;
    private String rawContent;
    private Long userId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ScheduleEntity create(String company, String position, LocalDateTime startTime,
                                         String meetingLink, String source, String rawContent) {
        var entity = new ScheduleEntity();
        entity.setCompany(company);
        entity.setPosition(position);
        entity.setStartTime(startTime);
        entity.setMeetingLink(meetingLink);
        entity.setStatus(InterviewScheduleStatus.PENDING.name());
        entity.setSource(source);
        entity.setRawContent(rawContent);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        return entity;
    }
}
```

- [ ] **Step 2: Create ScheduleMapper**

```java
// interview-backend/app/src/main/java/interview/backend/modules/interviewschedule/ScheduleMapper.java
package interview.backend.modules.interviewschedule;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import interview.backend.modules.interviewschedule.model.ScheduleEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ScheduleMapper extends BaseMapper<ScheduleEntity> {
}
```

- [ ] **Step 3: Refactor InterviewScheduleService**

```java
// interview-backend/app/src/main/java/interview/backend/modules/interviewschedule/InterviewScheduleService.java
package interview.backend.modules.interviewschedule;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import interview.backend.common.exception.BizException;
import interview.backend.infrastructure.ai.AiService;
import interview.backend.modules.interviewschedule.model.InterviewSchedule;
import interview.backend.modules.interviewschedule.model.InterviewScheduleStatus;
import interview.backend.modules.interviewschedule.model.ScheduleEntity;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class InterviewScheduleService {

    private static final Pattern LINK_PATTERN = Pattern.compile("(https?://\\S+)");
    private static final Pattern FULL_DATE_TIME_PATTERN = Pattern.compile("\\d{4}[-/]\\d{2}[-/]\\d{2}\\s+\\d{2}:\\d{2}");
    private static final Pattern SHORT_DATE_TIME_PATTERN = Pattern.compile("\\d{2}[-/]\\d{2}\\s+\\d{2}:\\d{2}");

    private final ScheduleMapper scheduleMapper;
    private final AiService aiService;

    public InterviewScheduleService(ScheduleMapper scheduleMapper, AiService aiService) {
        this.scheduleMapper = scheduleMapper;
        this.aiService = aiService;
    }

    public InterviewSchedule parseAndCreate(String content) {
        var extraction = extractWithAi(content);
        var entity = ScheduleEntity.create(
                extraction.company,
                extraction.position,
                extraction.startTime,
                extraction.meetingLink,
                extraction.source,
                content
        );
        scheduleMapper.insert(entity);
        return toSchedule(entity);
    }

    public List<InterviewSchedule> list() {
        return scheduleMapper.selectList(
                new QueryWrapper<ScheduleEntity>().orderByAsc("start_time")
        ).stream()
                .map(this::toSchedule)
                .toList();
    }

    public Map<String, Object> overview() {
        var total = scheduleMapper.selectCount(null);
        var pending = scheduleMapper.selectCount(
                new QueryWrapper<ScheduleEntity>().eq("status", InterviewScheduleStatus.PENDING.name())
        );
        var completed = scheduleMapper.selectCount(
                new QueryWrapper<ScheduleEntity>().eq("status", InterviewScheduleStatus.COMPLETED.name())
        );
        var canceled = scheduleMapper.selectCount(
                new QueryWrapper<ScheduleEntity>().eq("status", InterviewScheduleStatus.CANCELED.name())
        );
        var expired = scheduleMapper.selectCount(
                new QueryWrapper<ScheduleEntity>().eq("status", InterviewScheduleStatus.EXPIRED.name())
        );
        return Map.of(
                "total", total,
                "pending", pending,
                "completed", completed,
                "canceled", canceled,
                "expired", expired
        );
    }

    public InterviewSchedule updateStatus(Long id, InterviewScheduleStatus status) {
        var entity = scheduleMapper.selectById(id);
        if (entity == null) {
            throw new BizException("Interview schedule not found");
        }
        entity.setStatus(status.name());
        entity.setUpdatedAt(LocalDateTime.now());
        scheduleMapper.updateById(entity);
        return toSchedule(entity);
    }

    public Map<String, List<InterviewSchedule>> calendarView() {
        return scheduleMapper.selectList(
                new QueryWrapper<ScheduleEntity>().orderByAsc("start_time")
        ).stream()
                .map(this::toSchedule)
                .collect(Collectors.groupingBy(
                        schedule -> schedule.startTime().toLocalDate().toString(),
                        LinkedHashMap::new,
                        Collectors.toCollection(ArrayList::new)
                ));
    }

    private ScheduleExtraction extractWithAi(String content) {
        try {
            var aiResponse = aiService.chat("You are a schedule assistant. Extract company, position, meeting_link, and start_time (ISO format) from interview invitation text in JSON format.", content);
            return parseAiExtraction(aiResponse, content);
        } catch (Exception ex) {
            return extractWithRegex(content);
        }
    }

    private ScheduleExtraction parseAiExtraction(String aiResponse, String original) {
        try {
            var json = aiResponse;
            if (aiResponse.contains("{") && aiResponse.contains("}")) {
                int start = aiResponse.indexOf("{");
                int end = aiResponse.lastIndexOf("}") + 1;
                json = aiResponse.substring(start, end);
            }
            var parsed = JSON.parseObject(json);
            return new ScheduleExtraction(
                    parsed.getString("company"),
                    parsed.getString("position"),
                    parsed.getString("meetingLink"),
                    parseDateTime(parsed.getString("startTime")),
                    detectSource(original)
            );
        } catch (Exception ex) {
            return extractWithRegex(original);
        }
    }

    private ScheduleExtraction extractWithRegex(String content) {
        return new ScheduleExtraction(
                detectCompany(content),
                detectPosition(content),
                extractLink(content),
                detectStartTime(content),
                detectSource(content)
        );
    }

    private LocalDateTime parseDateTime(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (DateTimeParseException ex) {
            try {
                return LocalDateTime.parse(value);
            } catch (Exception e) {
                return LocalDateTime.now().plusDays(1);
            }
        }
    }

    private String detectCompany(String content) {
        var lower = content.toLowerCase();
        if (lower.contains("alibaba") || content.contains("阿里")) return "Alibaba";
        if (lower.contains("bytedance") || content.contains("字节")) return "ByteDance";
        if (lower.contains("tencent") || content.contains("腾讯")) return "Tencent";
        if (lower.contains("meituan") || content.contains("美团")) return "Meituan";
        return "Unknown Company";
    }

    private String detectPosition(String content) {
        var lower = content.toLowerCase();
        if (lower.contains("frontend") || content.contains("前端")) return "Frontend Engineer";
        if (lower.contains("python")) return "Python Engineer";
        if (lower.contains("algorithm") || content.contains("算法")) return "Algorithm Engineer";
        if (lower.contains("ai")) return "AI Engineer";
        return "Backend Engineer";
    }

    private LocalDateTime detectStartTime(String content) {
        var compact = content
                .replace("年", "-")
                .replace("月", "-")
                .replace("日", "")
                .replace("：", ":");

        var fullMatcher = FULL_DATE_TIME_PATTERN.matcher(compact);
        if (fullMatcher.find()) {
            var token = fullMatcher.group();
            try {
                return LocalDateTime.parse(token,
                        DateTimeFormatter.ofPattern(token.contains("/") ? "yyyy/MM/dd HH:mm" : "yyyy-MM-dd HH:mm"));
            } catch (DateTimeParseException ignored) {}
        }

        var shortMatcher = SHORT_DATE_TIME_PATTERN.matcher(compact);
        if (shortMatcher.find()) {
            var token = shortMatcher.group();
            try {
                return LocalDateTime.of(
                        LocalDate.now()
                                .withMonth(Integer.parseInt(token.substring(0, 2)))
                                .withDayOfMonth(Integer.parseInt(token.substring(3, 5))),
                        LocalTime.parse(token.substring(6))
                );
            } catch (Exception ignored) {}
        }
        return LocalDateTime.now().plusDays(1).withHour(19).withMinute(0).withSecond(0).withNano(0);
    }

    private String extractLink(String content) {
        var matcher = LINK_PATTERN.matcher(content);
        return matcher.find() ? matcher.group(1) : "No meeting link detected";
    }

    private String detectSource(String content) {
        var lower = content.toLowerCase();
        if (lower.contains("zoom")) return "Zoom";
        if (lower.contains("feishu") || content.contains("飞书")) return "Feishu";
        if (lower.contains("meeting") || content.contains("腾讯会议")) return "Tencent Meeting";
        return "General Invite";
    }

    private InterviewSchedule toSchedule(ScheduleEntity entity) {
        return new InterviewSchedule(
                entity.getId(),
                entity.getCompany(),
                entity.getPosition(),
                entity.getStartTime(),
                entity.getMeetingLink(),
                InterviewScheduleStatus.valueOf(entity.getStatus()),
                entity.getSource()
        );
    }

    private record ScheduleExtraction(
            String company,
            String position,
            String meetingLink,
            LocalDateTime startTime,
            String source
    ) {}
}
```

- [ ] **Step 4: Commit schedule module changes**

```bash
git add interview-backend/app/src/main/java/interview/backend/modules/interviewschedule/model/ScheduleEntity.java
git add interview-backend/app/src/main/java/interview/backend/modules/interviewschedule/ScheduleMapper.java
git add interview-backend/app/src/main/java/interview/backend/modules/interviewschedule/InterviewScheduleService.java
git commit -m "refactor(schedule): use ScheduleMapper and AiService instead of ConcurrentHashMap

Co-Authored-By: Claude Opus 4.7 <noreply@anthropic.com>"
```

---

## Task 11: Knowledge Module - Database Migration

**Files:**
- Create: `interview-backend/app/src/main/resources/db/migration/V5__Create_knowledge_document_table.sql`

- [ ] **Step 1: Create Flyway migration for knowledge table**

```sql
-- interview-backend/app/src/main/resources/db/migration/V5__Create_knowledge_document_table.sql
CREATE TABLE IF NOT EXISTS knowledge_document (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    summary TEXT,
    chunks INT DEFAULT 0,
    token_estimate INT DEFAULT 0,
    keywords JSON,
    full_content MEDIUMTEXT,
    user_id BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

- [ ] **Step 2: Commit migration**

```bash
git add interview-backend/app/src/main/resources/db/migration/V5__Create_knowledge_document_table.sql
git commit -m "feat(knowledge): add Flyway migration for knowledge_document table

Co-Authored-By: Claude Opus 4.7 <noreply@anthropic.com>"
```

---

## Task 12: Knowledge Module - Entity, Mapper, Service

**Files:**
- Create: `interview-backend/app/src/main/java/interview/backend/modules/knowledgebase/model/DocumentEntity.java`
- Create: `interview-backend/app/src/main/java/interview/backend/modules/knowledgebase/DocumentMapper.java`
- Modify: `interview-backend/app/src/main/java/interview/backend/modules/knowledgebase/KnowledgeBaseService.java`

- [ ] **Step 1: Create DocumentEntity**

```java
// interview-backend/app/src/main/java/interview/backend/modules/knowledgebase/model/DocumentEntity.java
package interview.backend.modules.knowledgebase.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import interview.backend.common.handler.JsonListTypeHandler;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("knowledge_document")
public class DocumentEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String fileName;
    private String filePath;
    private String summary;
    private Integer chunks;
    private Integer tokenEstimate;
    @TableField(typeHandler = JsonListTypeHandler.class)
    private List<String> keywords;
    private String fullContent;
    private Long userId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

- [ ] **Step 2: Create DocumentMapper**

```java
// interview-backend/app/src/main/java/interview/backend/modules/knowledgebase/DocumentMapper.java
package interview.backend.modules.knowledgebase;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import interview.backend.modules.knowledgebase.model.DocumentEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DocumentMapper extends BaseMapper<DocumentEntity> {
}
```

- [ ] **Step 3: Refactor KnowledgeBaseService**

```java
// interview-backend/app/src/main/java/interview/backend/modules/knowledgebase/KnowledgeBaseService.java
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
import java.util.stream.Collectors;
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
```

- [ ] **Step 4: Update KnowledgeBaseController to expose stream endpoint**

```java
// interview-backend/app/src/main/java/interview/backend/modules/knowledgebase/KnowledgeBaseController.java
package interview.backend.modules.knowledgebase;

import interview.backend.common.result.ApiResponse;
import interview.backend.modules.knowledgebase.model.KnowledgeDocument;
import java.util.List;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/knowledge")
public class KnowledgeBaseController {

    private final KnowledgeBaseService knowledgeBaseService;

    public KnowledgeBaseController(KnowledgeBaseService knowledgeBaseService) {
        this.knowledgeBaseService = knowledgeBaseService;
    }

    @PostMapping("/upload")
    public ApiResponse<KnowledgeDocument> upload(@RequestParam("file") MultipartFile file) {
        return ApiResponse.success("Document uploaded", knowledgeBaseService.upload(file));
    }

    @GetMapping
    public ApiResponse<List<KnowledgeDocument>> list() {
        return ApiResponse.success(knowledgeBaseService.list());
    }

    @GetMapping("/stats")
    public ApiResponse<Map<String, Object>> stats() {
        return ApiResponse.success(knowledgeBaseService.stats());
    }

    @GetMapping("/chat")
    public SseEmitter chat(@RequestParam String question) {
        return knowledgeBaseService.chat(question);
    }

    @GetMapping(value = "/chat-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> chatStream(@RequestParam String question) {
        return knowledgeBaseService.chatStream(question)
                .map(chunk -> ServerSentEvent.<String>builder()
                        .data(chunk)
                        .build());
    }

    @GetMapping("/search")
    public ApiResponse<List<KnowledgeDocument>> search(@RequestParam String question) {
        return ApiResponse.success(knowledgeBaseService.search(question));
    }

    @GetMapping("/{id}/download")
    public byte[] download(@PathVariable Long id) {
        return knowledgeBaseService.download(id);
    }
}
```

- [ ] **Step 5: Commit knowledge module changes**

```bash
git add interview-backend/app/src/main/java/interview/backend/modules/knowledgebase/model/DocumentEntity.java
git add interview-backend/app/src/main/java/interview/backend/modules/knowledgebase/DocumentMapper.java
git add interview-backend/app/src/main/java/interview/backend/modules/knowledgebase/KnowledgeBaseService.java
git add interview-backend/app/src/main/java/interview/backend/modules/knowledgebase/KnowledgeBaseController.java
git commit -m "refactor(knowledge): use DocumentMapper and AiService, add streaming endpoint

Co-Authored-By: Claude Opus 4.7 <noreply@anthropic.com>"
```

---

## Task 13: Voice Interview Module - Database Migration

**Files:**
- Create: `interview-backend/app/src/main/resources/db/migration/V6__Create_voice_interview_table.sql`

- [ ] **Step 1: Create Flyway migration for voice interview table**

```sql
-- interview-backend/app/src/main/resources/db/migration/V6__Create_voice_interview_table.sql
CREATE TABLE IF NOT EXISTS voice_interview_session (
    session_id VARCHAR(36) PRIMARY KEY,
    paused TINYINT(1) DEFAULT 0,
    latest_transcript TEXT,
    current_question TEXT,
    subtitles JSON,
    ai_replies JSON,
    submitted_turns INT DEFAULT 0,
    user_id BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

- [ ] **Step 2: Commit migration**

```bash
git add interview-backend/app/src/main/resources/db/migration/V6__Create_voice_interview_table.sql
git commit -m "feat(voice): add Flyway migration for voice_interview_session table

Co-Authored-By: Claude Opus 4.7 <noreply@anthropic.com>"
```

---

## Task 14: Voice Interview Module - Entity, Mapper, Service

**Files:**
- Create: `interview-backend/app/src/main/java/interview/backend/modules/voiceinterview/model/VoiceSessionEntity.java`
- Create: `interview-backend/app/src/main/java/interview/backend/modules/voiceinterview/VoiceSessionMapper.java`
- Modify: `interview-backend/app/src/main/java/interview/backend/modules/voiceinterview/VoiceInterviewService.java`

- [ ] **Step 1: Create VoiceSessionEntity**

```java
// interview-backend/app/src/main/java/interview/backend/modules/voiceinterview/model/VoiceSessionEntity.java
package interview.backend.modules.voiceinterview.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import interview.backend.common.handler.JsonListTypeHandler;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("voice_interview_session")
public class VoiceSessionEntity {
    @TableId
    private String sessionId;
    private Boolean paused;
    private String latestTranscript;
    private String currentQuestion;
    @TableField(typeHandler = JsonListTypeHandler.class)
    private List<String> subtitles;
    @TableField(typeHandler = JsonListTypeHandler.class)
    private List<String> aiReplies;
    private Integer submittedTurns;
    private Long userId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

- [ ] **Step 2: Create VoiceSessionMapper**

```java
// interview-backend/app/src/main/java/interview/backend/modules/voiceinterview/VoiceSessionMapper.java
package interview.backend.modules.voiceinterview;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import interview.backend.modules.voiceinterview.model.VoiceSessionEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface VoiceSessionMapper extends BaseMapper<VoiceSessionEntity> {
}
```

- [ ] **Step 3: Refactor VoiceInterviewService**

```java
// interview-backend/app/src/main/java/interview/backend/modules/voiceinterview/VoiceInterviewService.java
package interview.backend.modules.voiceinterview;

import interview.backend.common.exception.BizException;
import interview.backend.infrastructure.ai.AiService;
import interview.backend.infrastructure.export.PdfExportService;
import interview.backend.modules.voiceinterview.model.VoiceSessionEntity;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class VoiceInterviewService {

    private final VoiceSessionMapper sessionMapper;
    private final PdfExportService pdfExportService;
    private final AiService aiService;

    public VoiceInterviewService(
            VoiceSessionMapper sessionMapper,
            PdfExportService pdfExportService,
            AiService aiService
    ) {
        this.sessionMapper = sessionMapper;
        this.pdfExportService = pdfExportService;
        this.aiService = aiService;
    }

    public VoiceInterviewSession create() {
        var now = LocalDateTime.now();
        var entity = new VoiceSessionEntity();
        entity.setSessionId(UUID.randomUUID().toString());
        entity.setPaused(false);
        entity.setLatestTranscript("");
        entity.setCurrentQuestion("Please introduce yourself and summarize your most relevant project.");
        entity.setSubtitles(List.of());
        entity.setAiReplies(List.of("Voice interview session created."));
        entity.setSubmittedTurns(0);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        sessionMapper.insert(entity);
        return toSession(entity);
    }

    public List<VoiceInterviewSession> list() {
        return sessionMapper.selectList(null).stream()
                .sorted(Comparator.comparing(VoiceSessionEntity::getUpdatedAt).reversed())
                .map(this::toSession)
                .toList();
    }

    public VoiceInterviewSession detail(String sessionId) {
        var entity = sessionMapper.selectById(sessionId);
        if (entity == null) {
            throw new BizException("Voice interview session not found");
        }
        return toSession(entity);
    }

    public VoiceInterviewSession pause(String sessionId) {
        return mutate(sessionId, true, null, "Session paused.");
    }

    public VoiceInterviewSession resume(String sessionId) {
        return mutate(sessionId, false, null, "Session resumed.");
    }

    public VoiceInterviewSession submitText(String sessionId, String transcript) {
        var reply = buildAiReply(transcript);
        return mutate(sessionId, false, transcript, reply);
    }

    public Map<String, String> buildRealtimeReply(String sessionId, String payload) {
        var entity = sessionMapper.selectById(sessionId);
        if (entity == null) {
            throw new BizException("Voice interview session not found");
        }
        var subtitle = payload == null ? "" : payload.trim();
        var reply = buildAiReply(subtitle);
        entity.setPaused(false);
        if (!subtitle.isBlank()) {
            entity.setLatestTranscript(subtitle);
        }
        entity.setCurrentQuestion(nextQuestion(entity.getSubmittedTurns() + (subtitle.isBlank() ? 0 : 1)));
        entity.setSubtitles(append(entity.getSubtitles(), subtitle.isBlank() ? "empty-frame" : subtitle));
        entity.setAiReplies(append(entity.getAiReplies(), reply));
        entity.setSubmittedTurns(entity.getSubmittedTurns() + (subtitle.isBlank() ? 0 : 1));
        entity.setUpdatedAt(LocalDateTime.now());
        sessionMapper.updateById(entity);
        return Map.of(
                "subtitle", subtitle,
                "reply", reply,
                "question", entity.getCurrentQuestion()
        );
    }

    public byte[] exportReport(String sessionId) {
        var session = detail(sessionId);
        var body = """
                Session ID: %s
                Paused: %s
                Submitted Turns: %s
                Latest Transcript:
                %s

                Subtitles:
                %s

                AI Replies:
                %s
                """.formatted(
                session.sessionId(),
                session.paused(),
                session.submittedTurns(),
                session.latestTranscript(),
                String.join(" | ", session.subtitles()),
                String.join(" | ", session.aiReplies())
        );
        return pdfExportService.exportTextReport("Voice Interview Report", body);
    }

    private VoiceInterviewSession mutate(String sessionId, boolean paused, String transcript, String aiReply) {
        var entity = sessionMapper.selectById(sessionId);
        if (entity == null) {
            throw new BizException("Voice interview session not found");
        }
        var updatedTranscript = transcript == null || transcript.isBlank()
                ? entity.getLatestTranscript()
                : transcript;
        entity.setPaused(paused);
        entity.setLatestTranscript(updatedTranscript);
        if (transcript != null && !transcript.isBlank()) {
            entity.setSubtitles(append(entity.getSubtitles(), transcript));
        }
        if (aiReply != null && !aiReply.isBlank()) {
            entity.setAiReplies(append(entity.getAiReplies(), aiReply));
        }
        entity.setSubmittedTurns(transcript == null || transcript.isBlank()
                ? entity.getSubmittedTurns()
                : entity.getSubmittedTurns() + 1);
        entity.setUpdatedAt(LocalDateTime.now());
        sessionMapper.updateById(entity);
        return toSession(entity);
    }

    private String buildAiReply(String transcript) {
        if (transcript == null || transcript.isBlank()) {
            return "Please continue. I need more detail about your project and technical choices.";
        }
        // Use AI service for dynamic replies
        var aiResponse = aiService.chat(
                "You are a voice interview coach. Provide brief, encouraging follow-up questions.",
                "Candidate response: " + transcript
        );
        if (aiResponse != null && !aiResponse.isBlank()) {
            return aiResponse;
        }
        // Fallback logic
        var lower = transcript.toLowerCase();
        if (lower.contains("redis") || lower.contains("cache")) {
            return "Good. Why was Redis a better choice than local cache or database reads in that scenario?";
        }
        if (lower.contains("project")) {
            return "Please quantify the project impact with latency, throughput, stability, or delivery metrics.";
        }
        return "Please drill deeper into architecture, trade-offs, and measurable business impact.";
    }

    private String nextQuestion(long turns) {
        if (turns <= 1) {
            return "What was the hardest engineering decision in your recent project?";
        }
        if (turns == 2) {
            return "How did you verify correctness, stability, and observability after release?";
        }
        return "If you had one more sprint, what would you improve and why?";
    }

    private List<String> append(List<String> source, String value) {
        var copy = new ArrayList<>(source != null ? source : List.of());
        copy.add(value);
        return List.copyOf(copy);
    }

    private VoiceInterviewSession toSession(VoiceSessionEntity entity) {
        return new VoiceInterviewSession(
                entity.getSessionId(),
                Boolean.TRUE.equals(entity.getPaused()),
                entity.getLatestTranscript() != null ? entity.getLatestTranscript() : "",
                entity.getCurrentQuestion() != null ? entity.getCurrentQuestion() : "",
                entity.getSubtitles() != null ? entity.getSubtitles() : List.of(),
                entity.getAiReplies() != null ? entity.getAiReplies() : List.of(),
                entity.getSubmittedTurns() != null ? entity.getSubmittedTurns() : 0,
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
```

- [ ] **Step 4: Commit voice interview module changes**

```bash
git add interview-backend/app/src/main/java/interview/backend/modules/voiceinterview/model/VoiceSessionEntity.java
git add interview-backend/app/src/main/java/interview/backend/modules/voiceinterview/VoiceSessionMapper.java
git add interview-backend/app/src/main/java/interview/backend/modules/voiceinterview/VoiceInterviewService.java
git commit -m "refactor(voice): use VoiceSessionMapper and AiService instead of ConcurrentHashMap

Co-Authored-By: Claude Opus 4.7 <noreply@anthropic.com>"
```

---

## Task 15: Final Verification and Integration Test

**Files:**
- Modify: `interview-backend/app/src/main/resources/application-dev.yml`

- [ ] **Step 1: Run the application to verify all migrations apply**

```bash
cd interview-backend && mvn spring-boot:run
```

Expected: Application starts, Flyway applies V2-V6 migrations, all endpoints work.

- [ ] **Step 2: Test Resume upload and analysis**

```bash
curl -X POST -F "file=@test-resume.pdf" http://localhost:8080/api/resumes
curl http://localhost:8080/api/resumes
curl http://localhost:8080/api/resumes/stats
```

Expected: Resume uploads, status returns from database, analysis runs.

- [ ] **Step 3: Test Interview session creation and answering**

```bash
curl -X POST -H "Content-Type: application/json" \
  -d '{"direction":"JAVA_BACKEND","totalMinutes":45,"followUpRounds":1}' \
  http://localhost:8080/api/interviews
curl http://localhost:8080/api/interviews
curl -X POST -H "Content-Type: application/json" \
  -d '{"answer":"I have 5 years of Java experience..."}' \
  http://localhost:8080/api/interviews/1/answer
```

Expected: Session created, answered, persisted to database.

- [ ] **Step 4: Test Schedule parsing**

```bash
curl -X POST -H "Content-Type: application/json" \
  -d '{"content":"Dear candidate, you are invited to an interview at Alibaba on 2025-05-01 14:00. Meeting link: https://zoom.us/j/123"}' \
  http://localhost:8080/api/schedule/parse
curl http://localhost:8080/api/schedule
```

Expected: Schedule parsed, persisted, calendar view works.

- [ ] **Step 5: Final commit if any fixes needed**

```bash
git add -A
git commit -m "fix: resolve any integration issues from database migration

Co-Authored-By: Claude Opus 4.7 <noreply@anthropic.com>"
```

---

## Summary

This plan transforms the Interview Assistant from an in-memory prototype to a production-ready system:

1. **AI Service Layer** - Interface for future AI providers, stub for current dev
2. **Resume Module** - Full database persistence with MyBatis-Plus
3. **Interview Module** - Session + Question tables with relationships
4. **Schedule Module** - Invitation parsing with AI extraction
5. **Knowledge Module** - Document storage with RAG pattern
6. **Voice Interview** - Real-time session persistence

All modules maintain their existing API contracts, so frontend requires no changes. Each migration is incremental and reversible.
