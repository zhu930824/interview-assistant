package interview.backend.modules.resume;

import interview.backend.common.result.ApiResponse;
import interview.backend.modules.resume.dto.ResumeUploadResponse;
import interview.backend.modules.resume.model.ResumeRecord;
import java.util.List;
import java.util.Map;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/resumes")
public class ResumeController {

    private final ResumeService resumeService;

    public ResumeController(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<ResumeUploadResponse> upload(@RequestParam("file") MultipartFile file) {
        return ApiResponse.success("Upload successful", resumeService.upload(file));
    }

    @GetMapping
    public ApiResponse<List<ResumeRecord>> list() {
        return ApiResponse.success(resumeService.list());
    }

    @GetMapping("/stats")
    public ApiResponse<Map<String, Object>> stats() {
        return ApiResponse.success(resumeService.stats());
    }

    @GetMapping("/{id}")
    public ApiResponse<ResumeRecord> detail(@PathVariable Long id) {
        return ApiResponse.success(resumeService.detail(id));
    }

    @PostMapping("/{id}/retry")
    public ApiResponse<ResumeRecord> retry(@PathVariable Long id) {
        return ApiResponse.success("Retry scheduled", resumeService.retry(id));
    }

    @GetMapping("/{id}/report")
    public ResponseEntity<byte[]> export(@PathVariable Long id) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename("resume-report-" + id + ".pdf").build().toString())
                .contentType(MediaType.APPLICATION_PDF)
                .body(resumeService.exportReport(id));
    }
}
