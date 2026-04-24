package interview.backend.modules.interviewschedule;

import interview.backend.modules.interviewschedule.model.InterviewScheduleStatus;
import java.time.LocalDateTime;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class InterviewScheduleJob {

    private final InterviewScheduleService interviewScheduleService;

    public InterviewScheduleJob(InterviewScheduleService interviewScheduleService) {
        this.interviewScheduleService = interviewScheduleService;
    }

    @Scheduled(fixedDelay = 60_000L)
    public void expireOldSchedules() {
        interviewScheduleService.list().stream()
                .filter(schedule -> schedule.status() == InterviewScheduleStatus.PENDING)
                .filter(schedule -> schedule.startTime().isBefore(LocalDateTime.now().minusHours(2)))
                .forEach(schedule -> interviewScheduleService.updateStatus(schedule.id(), InterviewScheduleStatus.EXPIRED));
    }
}
