package interview.backend.infrastructure.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class LocalStorageService {

    private final Path basePath;

    public LocalStorageService(@Value("${app.storage.base-path}") String basePath) {
        this.basePath = Path.of(basePath);
    }

    public String save(MultipartFile file, String prefix) throws IOException {
        Files.createDirectories(basePath.resolve(prefix));
        var filename = UUID.randomUUID() + "-" + file.getOriginalFilename();
        var target = basePath.resolve(prefix).resolve(filename);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        return target.toString();
    }
}
