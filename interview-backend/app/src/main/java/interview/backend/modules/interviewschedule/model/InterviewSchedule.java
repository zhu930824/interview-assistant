package interview.backend.modules.interviewschedule.model;

import java.time.LocalDateTime;

public record InterviewSchedule(
        Long id,
        String company,
        String position,
        LocalDateTime startTime,
        String meetingLink,
        InterviewScheduleStatus status,
        String source
) {
}
