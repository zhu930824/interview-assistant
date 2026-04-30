package interview.backend.infrastructure.file;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * 文档文本提取器
 * 支持 PDF、DOCX、TXT、MD 等格式
 */
@Component
public class DocumentTextExtractor {

    private static final Logger log = LoggerFactory.getLogger(DocumentTextExtractor.class);

    /**
     * 从文档中提取文本内容
     */
    public String extract(InputStream inputStream, String fileName) throws IOException {
        if (fileName == null) {
            return readAsPlainText(inputStream);
        }

        String lowerName = fileName.toLowerCase();

        if (lowerName.endsWith(".pdf")) {
            return extractPdf(inputStream);
        }

        if (lowerName.endsWith(".docx")) {
            return extractDocx(inputStream.readAllBytes());
        }

        if (lowerName.endsWith(".doc")) {
            log.warn("DOC format detected, limited support - attempting PDF extraction");
            return extractPdf(inputStream);
        }

        if (lowerName.endsWith(".md") || lowerName.endsWith(".txt")) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }

        return readAsPlainText(inputStream);
    }

    /**
     * 使用 PDFBox 提取 PDF 文本
     */
    private String extractPdf(InputStream inputStream) throws IOException {
        byte[] data = inputStream.readAllBytes();
        try (PDDocument document = Loader.loadPDF(data)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            String text = stripper.getText(document);
            log.info("Extracted {} characters from PDF", text.length());
            return text.trim();
        } catch (Exception e) {
            log.error("Failed to extract PDF text: {}", e.getMessage());
            throw new IOException("PDF 文本提取失败: " + e.getMessage(), e);
        }
    }

    /**
     * 使用 POI 提取 DOCX 文本
     */
    private String extractDocx(byte[] data) throws IOException {
        try (XWPFDocument document = new XWPFDocument(new ByteArrayInputStream(data))) {
            StringBuilder sb = new StringBuilder();
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                String text = paragraph.getText();
                if (text != null && !text.isBlank()) {
                    sb.append(text).append("\n");
                }
            }
            log.info("Extracted {} characters from DOCX", sb.length());
            return sb.toString().trim();
        } catch (Exception e) {
            log.error("Failed to extract DOCX text: {}", e.getMessage());
            throw new IOException("DOCX 文本提取失败: " + e.getMessage(), e);
        }
    }

    private String readAsPlainText(InputStream inputStream) throws IOException {
        return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8).trim();
    }
}