package interview.backend.modules.interviewschedule;

import interview.backend.common.exception.BizException;
import interview.backend.modules.interviewschedule.model.InterviewSchedule;
import interview.backend.modules.interviewschedule.model.InterviewScheduleStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

@Service
public class InterviewScheduleService {

    private static final Pattern LINK_PATTERN = Pattern.compile("(https?://\\S+)");
    private static final Pattern FULL_DATE_TIME_PATTERN = Pattern.compile("\\d{4}[-/]\\d{2}[-/]\\d{2}\\s+\\d{2}:\\d{2}");
    private static final Pattern SHORT_DATE_TIME_PATTERN = Pattern.compile("\\d{2}[-/]\\d{2}\\s+\\d{2}:\\d{2}");

    private final AtomicLong idGenerator = new AtomicLong(1);
    private final Map<Long, InterviewSchedule> schedules = new ConcurrentHashMap<>();

    public InterviewSchedule parseAndCreate(String content) {
        var schedule = new InterviewSchedule(
                idGenerator.getAndIncrement(),
                detectCompany(content),
                detectPosition(content),
                detectStartTime(content),
                extractLink(content),
                InterviewScheduleStatus.PENDING,
                detectSource(content)
        );
        schedules.put(schedule.id(), schedule);
        return schedule;
    }

    public List<InterviewSchedule> list() {
        return schedules.values().stream()
                .sorted(Comparator.comparing(InterviewSchedule::startTime))
                .toList();
    }

    public Map<String, Object> overview() {
        var result = new java.util.LinkedHashMap<String, Object>();
        result.put("total", schedules.size());
        result.put("pending", schedules.values().stream().filter(item -> item.status() == InterviewScheduleStatus.PENDING).count());
        result.put("completed", schedules.values().stream().filter(item -> item.status() == InterviewScheduleStatus.COMPLETED).count());
        result.put("canceled", schedules.values().stream().filter(item -> item.status() == InterviewScheduleStatus.CANCELED).count());
        result.put("expired", schedules.values().stream().filter(item -> item.status() == InterviewScheduleStatus.EXPIRED).count());
        return result;
    }

    public InterviewSchedule updateStatus(Long id, InterviewScheduleStatus status) {
        var schedule = schedules.get(id);
        if (schedule == null) {
            throw new BizException("Interview schedule not found");
        }
        var updated = new InterviewSchedule(
                schedule.id(),
                schedule.company(),
                schedule.position(),
                schedule.startTime(),
                schedule.meetingLink(),
                status,
                schedule.source()
        );
        schedules.put(id, updated);
        return updated;
    }

    public Map<String, List<InterviewSchedule>> calendarView() {
        return schedules.values().stream()
                .sorted(Comparator.comparing(InterviewSchedule::startTime))
                .collect(java.util.stream.Collectors.groupingBy(
                        schedule -> schedule.startTime().toLocalDate().toString(),
                        java.util.LinkedHashMap::new,
                        java.util.stream.Collectors.toCollection(ArrayList::new)
                ));
    }

    private String detectCompany(String content) {
        var lower = content.toLowerCase();
        if (lower.contains("alibaba") || content.contains("\u963f\u91cc")) {
            return "Alibaba";
        }
        if (lower.contains("bytedance") || content.contains("\u5b57\u8282")) {
            return "ByteDance";
        }
        if (lower.contains("tencent") || content.contains("\u817e\u8baf")) {
            return "Tencent";
        }
        if (lower.contains("meituan") || content.contains("\u7f8e\u56e2")) {
            return "Meituan";
        }
        return "Unknown Company";
    }

    private String detectPosition(String content) {
        var lower = content.toLowerCase();
        if (lower.contains("frontend") || content.contains("\u524d\u7aef")) {
            return "Frontend Engineer";
        }
        if (lower.contains("python")) {
            return "Python Engineer";
        }
        if (lower.contains("algorithm") || content.contains("\u7b97\u6cd5")) {
            return "Algorithm Engineer";
        }
        if (lower.contains("ai")) {
            return "AI Engineer";
        }
        return "Backend Engineer";
    }

    private LocalDateTime detectStartTime(String content) {
        var compact = content
                .replace("\u5e74", "-")
                .replace("\u6708", "-")
                .replace("\u65e5", "")
                .replace("\uff1a", ":");

        var fullMatcher = FULL_DATE_TIME_PATTERN.matcher(compact);
        if (fullMatcher.find()) {
            var token = fullMatcher.group();
            try {
                return LocalDateTime.parse(
                        token,
                        DateTimeFormatter.ofPattern(token.contains("/") ? "yyyy/MM/dd HH:mm" : "yyyy-MM-dd HH:mm")
                );
            } catch (DateTimeParseException ignored) {
            }
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
            } catch (Exception ignored) {
            }
        }
        return LocalDateTime.now().plusDays(1).withHour(19).withMinute(0).withSecond(0).withNano(0);
    }

    private String extractLink(String content) {
        var matcher = LINK_PATTERN.matcher(content);
        return matcher.find() ? matcher.group(1) : "No meeting link detected";
    }

    private String detectSource(String content) {
        var lower = content.toLowerCase();
        if (lower.contains("zoom")) {
            return "Zoom";
        }
        if (lower.contains("feishu") || content.contains("\u98de\u4e66")) {
            return "Feishu";
        }
        if (lower.contains("meeting") || content.contains("\u817e\u8baf\u4f1a\u8bae")) {
            return "Tencent Meeting";
        }
        return "General Invite";
    }
}
