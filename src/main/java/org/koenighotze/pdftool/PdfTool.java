package org.koenighotze.pdftool;

import static java.lang.String.format;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.newInputStream;
import static java.nio.file.StandardOpenOption.WRITE;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.AcroFields;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;

/**
 * Simple tool for pre-stamping a PDF form with the keys of the fields as their
 * value.
 *
 * @author dschmitz
 */
public class PdfTool {
    private final Path document;

    public PdfTool(Path path) {
        this.document = path;
    }

    public static void main(String[] args) throws IOException, DocumentException {
        if (args.length != 1) {
            printUsage();

            return;
        }

        Path path = Paths.get(args[0]);
        if (!exists(path)) {
            System.err.println(format("File %s does not exist!", path.getFileName()));

            return;
        }

        new PdfTool(path).printPreFilledPdf();
    }

    private byte[] prefill() throws IOException, DocumentException {
        PdfReader pdfReader = null;
        PdfStamper stamper = null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            pdfReader = new PdfReader(newInputStream(document));
            stamper = new PdfStamper(pdfReader, baos);
            stamper.setFormFlattening(true);

            final AtomicInteger i = new AtomicInteger();

            AcroFields fields = stamper.getAcroFields();
            @SuppressWarnings("unchecked") Set<String> set = fields.getFields().keySet();
            set.stream().forEach(key -> {
                try {
                    System.out.println("Stamping key " + key + " as " + i.intValue());
                    fields.setField(key, i.intValue() + "");
                    i.incrementAndGet();
                } catch (IOException | DocumentException e) {
                    System.err.println(format("Cannot stamp field %s", key));
                }
            });
            baos.flush();
        } finally {
            closeStamper(stamper);
            closeReader(pdfReader);
        }

        return baos.toByteArray();
    }

    private void closeReader(PdfReader pdfReader) {
        if (pdfReader != null) {
            pdfReader.close();
        }
    }

    private void closeStamper(PdfStamper stamper) {
        if (stamper != null) {
            try {
                stamper.close();
            } catch (DocumentException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void printPreFilledPdf() throws IOException, DocumentException {
        byte[] doc = prefill();

        Path out = Files.createTempFile("stamped", ".pdf");
        Files.write(out, doc, WRITE);
        System.out.println(format("Result is here: %s", out.toAbsolutePath()));
    }

    private static void printUsage() {
        System.out.printf("Usage: PdfTool <file.pdf>");
    }
}
