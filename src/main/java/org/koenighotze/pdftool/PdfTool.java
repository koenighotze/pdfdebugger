package org.koenighotze.pdftool;

import org.koenighotze.pdftool.stamper.Stamper;

import java.io.IOException;
import java.nio.file.Path;

import static java.nio.file.Files.*;

public class PdfTool {
    public Path printPreFilledPdf(Path documentPath, Path targetPath) throws IOException {
        if (!exists(documentPath)) {
            throw new IOException("PDF file does not exist!");
        }
        var out = createTempFile(targetPath, "stamped", ".pdf");

        try (var os = newOutputStream(out)) {
            new Stamper().prefill(readAllBytes(documentPath), os);
        }

        return out.toAbsolutePath();
    }
}
