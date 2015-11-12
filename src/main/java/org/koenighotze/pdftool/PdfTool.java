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
        if (args.length < 1 || args.length > 2) {
            printUsage();

            return;
        }
        boolean usenum = false;
        if (args.length == 2) {
            usenum = "usenum".equals(args[1]);
        }

        Path path = Paths.get(args[0]);
        if (!exists(path)) {
            System.err.println(format("File %s does not exist!", path.getFileName()));

            return;
        }

        new PdfTool(path).printPreFilledPdf(usenum);
    }

    private byte[] prefill(boolean usenum) throws IOException, DocumentException {
        PdfReader pdfReader = null;
        PdfStamper stamper = null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            pdfReader = new PdfReader(newInputStream(document));
            stamper = new PdfStamper(pdfReader, baos);
            stamper.setFormFlattening(true);

            stampFields(stamper, usenum);
            baos.flush();
        } finally {
            closeStamper(stamper);
            closeReader(pdfReader);
        }

        return baos.toByteArray();
    }

    private void stampFields(PdfStamper stamper, boolean usenum) {
        final AtomicInteger i = new AtomicInteger();

        AcroFields fields = stamper.getAcroFields();
        @SuppressWarnings("unchecked") Set<String> set = fields.getFields().keySet();
        set.stream().forEach(key -> {
            try {
                String val = usenum ? i.intValue() + "" : key;

                System.out.println("Stamping key " + key + " as " + val);
                fields.setField(key, val);
                i.incrementAndGet();
            } catch (IOException | DocumentException e) {
                System.err.println(format("Cannot stamp field %s", key));
            }
        });
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

    private void printPreFilledPdf(boolean usenum) throws IOException, DocumentException {
        byte[] doc = prefill(usenum);

        Path out = Files.createTempFile("stamped", ".pdf");
        Files.write(out, doc, WRITE);
        System.out.println(format("Result is here: %s", out.toAbsolutePath()));
    }

    private static void printUsage() {
        System.out.printf("Usage: PdfTool <file.pdf> [usenum]");
    }
}
