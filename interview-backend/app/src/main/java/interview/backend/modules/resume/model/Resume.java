package interview.backend.modules.resume.model;

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
    @TableField(typeHandler = JsonListTypeHandler.class)
    private List<String> keywords;
    @TableField(typeHandler = JsonListTypeHandler.class)
    private List<String> strengths;
    @TableField(typeHandler = JsonListTypeHandler.class)
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
