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
