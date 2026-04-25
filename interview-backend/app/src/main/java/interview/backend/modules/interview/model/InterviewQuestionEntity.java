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
