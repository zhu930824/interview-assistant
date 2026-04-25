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
