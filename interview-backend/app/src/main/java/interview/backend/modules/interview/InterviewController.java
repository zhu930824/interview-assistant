package interview.backend.modules.interview;

import interview.backend.common.result.ApiResponse;
import interview.backend.modules.interview.dto.CreateInterviewSessionRequest;
import interview.backend.modules.interview.dto.InterviewAnswerRequest;
import interview.backend.modules.interview.model.InterviewSession;
import jakarta.validation.Valid;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/interviews")
public class InterviewController {

    private final InterviewService interviewService;

    public InterviewController(InterviewService interviewService) {
        this.interviewService = interviewService;
    }

    @PostMapping
    public ApiResponse<InterviewSession> create(@Valid @RequestBody CreateInterviewSessionRequest request) {
        return ApiResponse.success("Interview created", interviewService.create(request));
    }

    @GetMapping
    public ApiResponse<List<InterviewSession>> list() {
        return ApiResponse.success(interviewService.list());
    }

    @GetMapping("/directions")
    public ApiResponse<List<Map<String, String>>> directions() {
        return ApiResponse.success(interviewService.directions());
    }

    @GetMapping("/center")
    public ApiResponse<Map<String, Object>> center() {
        var data = new LinkedHashMap<String, Object>();
        data.put("summary", interviewService.centerSummary());
        data.put("sessions", interviewService.list());
        return ApiResponse.success(data);
    }

    @GetMapping("/{id}")
    public ApiResponse<InterviewSession> detail(@PathVariable Long id) {
        return ApiResponse.success(interviewService.continueSession(id));
    }

    @PostMapping("/{id}/answer")
    public ApiResponse<InterviewSession> answer(@PathVariable Long id, @Valid @RequestBody InterviewAnswerRequest request) {
        return ApiResponse.success(interviewService.answer(id, request.answer()));
    }

    @PostMapping("/{id}/evaluate")
    public ApiResponse<InterviewSession> evaluate(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return ApiResponse.success(interviewService.evaluate(id, body.get("transcript")));
    }

    @GetMapping("/{id}/report")
    public ResponseEntity<byte[]> export(@PathVariable Long id) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename("interview-report-" + id + ".pdf").build().toString())
                .contentType(MediaType.APPLICATION_PDF)
                .body(interviewService.exportReport(id));
    }
}
