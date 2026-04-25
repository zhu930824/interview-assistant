package interview.backend.modules.interviewschedule;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private static final ObjectMapper MAPPER = new ObjectMapper();

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

    @SuppressWarnings("unchecked")
    private ScheduleExtraction parseAiExtraction(String aiResponse, String original) {
        try {
            var json = aiResponse;
            if (aiResponse.contains("{") && aiResponse.contains("}")) {
                int start = aiResponse.indexOf("{");
                int end = aiResponse.lastIndexOf("}") + 1;
                json = aiResponse.substring(start, end);
            }
            var parsed = MAPPER.readValue(json, Map.class);
            return new ScheduleExtraction(
                    (String) parsed.get("company"),
                    (String) parsed.get("position"),
                    (String) parsed.get("meetingLink"),
                    parseDateTime((String) parsed.get("startTime")),
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
