package interview.backend.infrastructure.export;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import org.springframework.stereotype.Service;

@Service
public class PdfExportService {

    public byte[] exportTextReport(String title, String body) {
        var text = escape(title + "\n\n" + body);
        var output = new ByteArrayOutputStream();
        var objects = new String[] {
                "1 0 obj << /Type /Catalog /Pages 2 0 R >> endobj\n",
                "2 0 obj << /Type /Pages /Kids [3 0 R] /Count 1 >> endobj\n",
                "3 0 obj << /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Contents 4 0 R /Resources << /Font << /F1 5 0 R >> >> >> endobj\n",
                "4 0 obj << /Length " + (text.length() + 40) + " >> stream\nBT\n/F1 12 Tf\n50 790 Td\n(" + text + ") Tj\nET\nendstream endobj\n",
                "5 0 obj << /Type /Font /Subtype /Type1 /BaseFont /Helvetica >> endobj\n"
        };
        output.writeBytes("%PDF-1.4\n".getBytes(StandardCharsets.US_ASCII));
        var offsets = new int[objects.length + 1];
        for (int i = 0; i < objects.length; i++) {
            offsets[i + 1] = output.size();
            output.writeBytes(objects[i].getBytes(StandardCharsets.US_ASCII));
        }
        var xrefOffset = output.size();
        output.writeBytes(("xref\n0 " + (objects.length + 1) + "\n").getBytes(StandardCharsets.US_ASCII));
        output.writeBytes("0000000000 65535 f \n".getBytes(StandardCharsets.US_ASCII));
        for (int i = 1; i <= objects.length; i++) {
            output.writeBytes(String.format("%010d 00000 n %n", offsets[i]).getBytes(StandardCharsets.US_ASCII));
        }
        output.writeBytes(("trailer << /Size " + (objects.length + 1) + " /Root 1 0 R >>\nstartxref\n" + xrefOffset + "\n%%EOF").getBytes(StandardCharsets.US_ASCII));
        return output.toByteArray();
    }

    private String escape(String raw) {
        return raw
                .replace("\\", "\\\\")
                .replace("(", "\\(")
                .replace(")", "\\)")
                .replace("\r", "")
                .replace("\n", ") Tj\n0 -16 Td\n(");
    }
}
