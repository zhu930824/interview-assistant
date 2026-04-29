package interview.backend.modules.voiceinterview;

import interview.backend.common.result.ApiResponse;
import interview.backend.infrastructure.speech.AlibabaSpeechService;
import interview.backend.infrastructure.speech.SpeechRecognitionService;
import interview.backend.infrastructure.speech.StubSpeechService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/voice-interviews")
public class VoiceInterviewController {

    private final VoiceInterviewService service;
    private final SpeechRecognitionService speechService;

    public VoiceInterviewController(VoiceInterviewService service,
                                     AlibabaSpeechService alibabaSpeechService,
                                     StubSpeechService stubSpeechService) {
        this.service = service;
        // 如果配置了阿里百炼 API key，使用真实服务，否则使用 stub
        this.speechService = alibabaSpeechService.isConfigured()
                ? alibabaSpeechService
                : stubSpeechService;
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

    @PostMapping("/transcribe")
    public ApiResponse<Map<String, String>> transcribe(@RequestParam("audio") MultipartFile audioFile) {
        try {
            byte[] audioData = audioFile.getBytes();
            String filename = audioFile.getOriginalFilename();
            String text = speechService.transcribe(audioData, filename);
            return ApiResponse.success(Map.of("text", text));
        } catch (Exception e) {
            return ApiResponse.error(500, "语音识别失败: " + e.getMessage());
        }
    }

    @GetMapping("/{sessionId}/report")
    public ResponseEntity<byte[]> report(@PathVariable String sessionId) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename("voice-interview-" + sessionId + ".pdf").build().toString())
                .contentType(MediaType.APPLICATION_PDF)
                .body(service.exportReport(sessionId));
    }
}
