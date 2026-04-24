package interview.backend.infrastructure.file;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;
import java.util.zip.ZipInputStream;
import org.springframework.stereotype.Component;

@Component
public class DocumentTextExtractor {

    public String extract(InputStream inputStream, String fileName) throws IOException {
        var lowerName = fileName == null ? "" : fileName.toLowerCase();
        if (lowerName.endsWith(".docx")) {
            return extractDocx(inputStream.readAllBytes());
        }
        if (lowerName.endsWith(".md") || lowerName.endsWith(".txt")) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
        if (lowerName.endsWith(".pdf") || lowerName.endsWith(".doc")) {
            var raw = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            return raw.replaceAll("[^\\p{L}\\p{N}\\p{Punct}\\s]", " ").replaceAll("\\s+", " ").trim();
        }
        return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
    }

    private String extractDocx(byte[] bytes) throws IOException {
        try (var zipInputStream = new ZipInputStream(new ByteArrayInputStream(bytes))) {
            java.util.zip.ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                if ("word/document.xml".equals(entry.getName())) {
                    var xml = new String(zipInputStream.readAllBytes(), StandardCharsets.UTF_8);
                    var noTags = Pattern.compile("<[^>]+>").matcher(xml).replaceAll(" ");
                    return noTags.replaceAll("\\s+", " ").trim();
                }
            }
        }
        return "";
    }
}
