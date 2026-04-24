package interview.backend.modules.interviewschedule;

import interview.backend.common.result.ApiResponse;
import interview.backend.modules.interviewschedule.dto.ParseInvitationRequest;
import interview.backend.modules.interviewschedule.model.InterviewSchedule;
import interview.backend.modules.interviewschedule.model.InterviewScheduleStatus;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/interview-schedules")
public class InterviewScheduleController {

    private final InterviewScheduleService service;

    public InterviewScheduleController(InterviewScheduleService service) {
        this.service = service;
    }

    @PostMapping("/parse")
    public ApiResponse<InterviewSchedule> parse(@Valid @RequestBody ParseInvitationRequest request) {
        return ApiResponse.success("Invitation parsed", service.parseAndCreate(request.content()));
    }

    @GetMapping
    public ApiResponse<List<InterviewSchedule>> list() {
        return ApiResponse.success(service.list());
    }

    @GetMapping("/overview")
    public ApiResponse<Map<String, Object>> overview() {
        return ApiResponse.success(service.overview());
    }

    @GetMapping("/calendar")
    public ApiResponse<Map<String, List<InterviewSchedule>>> calendar() {
        return ApiResponse.success(service.calendarView());
    }

    @PostMapping("/{id}/status")
    public ApiResponse<InterviewSchedule> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        var status = InterviewScheduleStatus.valueOf(body.getOrDefault("status", "PENDING"));
        return ApiResponse.success(service.updateStatus(id, status));
    }
}
