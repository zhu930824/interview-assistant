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
