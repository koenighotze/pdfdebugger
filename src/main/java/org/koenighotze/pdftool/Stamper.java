package org.koenighotze.pdftool;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.atomic.*;

import static java.lang.String.*;
import static java.nio.file.Files.*;
import static java.nio.file.StandardOpenOption.*;
import static java.util.Objects.*;

/**
 * Stamper for acrofields in pdf forms.
 *
 * @author dschmitz
 */
public class Stamper {
    private final Path document;

    public Stamper(Path path) {
        this.document = requireNonNull(path);
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

    public Path printPreFilledPdf(boolean usenum) throws IOException, DocumentException {
        byte[] doc = prefill(usenum);

        Path out = createTempFile("stamped", ".pdf");
        write(out, doc, WRITE);
        System.out.println(format("Result is here: %s", out.toAbsolutePath()));

        return out.toAbsolutePath();
    }

}
