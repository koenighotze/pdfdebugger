package org.koenighotze.pdftool;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.koenighotze.pdftool.stamper.Stamper;

import java.io.IOException;
import java.nio.file.Path;

import static java.nio.file.Files.*;
import static java.nio.file.Files.write;
import static java.nio.file.StandardOpenOption.WRITE;

/**
 * Simple tool for pre-stamping a PDF form with the keys of the fields as their
 * value.
 *
 * @author dschmitz
 */
public class PdfTool {
    private static final Logger LOGGER = LogManager.getLogger(PdfTool.class);

    public Path printPreFilledPdf(Path documentPath) throws IOException {
        byte[] fileContent = readAllBytes(documentPath);
        byte[] doc = new Stamper().prefill(fileContent);
        String outputDir = System.getenv("OUTPUT_DIR");
        if (outputDir == null) {
            outputDir = System.getProperty("java.io.tmpdir");
        }

        Path out = createTempFile(Path.of(outputDir), "stamped", ".pdf");
        write(out, doc, WRITE);

        return out.toAbsolutePath();
    }
}
