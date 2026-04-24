package interview.backend.modules.voiceinterview;

import interview.backend.common.result.ApiResponse;
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
@RequestMapping("/api/voice-interviews")
public class VoiceInterviewController {

    private final VoiceInterviewService service;

    public VoiceInterviewController(VoiceInterviewService service) {
        this.service = service;
    }

    @PostMapping
    public ApiResponse<VoiceInterviewSession> create() {
        return ApiResponse.success(service.create());
    }

    @GetMapping
    public ApiResponse<java.util.List<VoiceInterviewSession>> list() {
        return ApiResponse.success(service.list());
    }

    @GetMapping("/{sessionId}")
    public ApiResponse<VoiceInterviewSession> detail(@PathVariable String sessionId) {
        return ApiResponse.success(service.detail(sessionId));
    }

    @PostMapping("/{sessionId}/pause")
    public ApiResponse<VoiceInterviewSession> pause(@PathVariable String sessionId) {
        return ApiResponse.success(service.pause(sessionId));
    }

    @PostMapping("/{sessionId}/resume")
    public ApiResponse<VoiceInterviewSession> resume(@PathVariable String sessionId) {
        return ApiResponse.success(service.resume(sessionId));
    }

    @PostMapping("/{sessionId}/submit")
    public ApiResponse<VoiceInterviewSession> submit(@PathVariable String sessionId, @RequestBody Map<String, String> body) {
        return ApiResponse.success(service.submitText(sessionId, body.getOrDefault("transcript", "")));
    }

    @GetMapping("/{sessionId}/report")
    public ResponseEntity<byte[]> report(@PathVariable String sessionId) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename("voice-interview-" + sessionId + ".pdf").build().toString())
                .contentType(MediaType.APPLICATION_PDF)
                .body(service.exportReport(sessionId));
    }
}
