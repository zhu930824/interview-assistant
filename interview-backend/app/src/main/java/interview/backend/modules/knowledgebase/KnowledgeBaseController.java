package interview.backend.modules.knowledgebase;

import interview.backend.common.result.ApiResponse;
import interview.backend.modules.knowledgebase.model.KnowledgeDocument;
import java.util.List;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/knowledge")
public class KnowledgeBaseController {

    private final KnowledgeBaseService service;

    public KnowledgeBaseController(KnowledgeBaseService service) {
        this.service = service;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<KnowledgeDocument> upload(@RequestParam("file") MultipartFile file) {
        return ApiResponse.success("Upload successful", service.upload(file));
    }

    @GetMapping
    public ApiResponse<List<KnowledgeDocument>> list() {
        return ApiResponse.success(service.list());
    }

    @GetMapping("/stats")
    public ApiResponse<Map<String, Object>> stats() {
        return ApiResponse.success(service.stats());
    }

    @GetMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chat(@RequestParam("question") String question) {
        return service.chat(question);
    }

    @GetMapping("/{id}/download")
    public byte[] download(@PathVariable Long id) {
        return service.download(id);
    }
}
