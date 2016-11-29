package org.koenighotze.pdftool;

import static com.lowagie.text.pdf.AcroFields.FIELD_TYPE_CHECKBOX;
import static com.lowagie.text.pdf.AcroFields.FIELD_TYPE_COMBO;
import static com.lowagie.text.pdf.AcroFields.FIELD_TYPE_LIST;
import static com.lowagie.text.pdf.AcroFields.FIELD_TYPE_NONE;
import static com.lowagie.text.pdf.AcroFields.FIELD_TYPE_PUSHBUTTON;
import static com.lowagie.text.pdf.AcroFields.FIELD_TYPE_RADIOBUTTON;
import static com.lowagie.text.pdf.AcroFields.FIELD_TYPE_SIGNATURE;
import static com.lowagie.text.pdf.AcroFields.FIELD_TYPE_TEXT;
import static java.lang.String.format;
import static java.nio.file.Files.createTempFile;
import static java.nio.file.Files.newInputStream;
import static java.nio.file.Files.write;
import static java.nio.file.StandardOpenOption.WRITE;
import static java.util.Objects.requireNonNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.AcroFields;
import com.lowagie.text.pdf.PRAcroForm;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;

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

    private byte[] prefill(boolean usenum, boolean verbose) throws IOException, DocumentException {
        PdfReader pdfReader = null;
        PdfStamper stamper = null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            pdfReader = new PdfReader(newInputStream(document));
            PRAcroForm acroForm = pdfReader.getAcroForm();
            System.out.println(format("PDF is in Version %s and has %s pages", pdfReader.getPdfVersion(), pdfReader.getNumberOfPages()));

            stamper = new PdfStamper(pdfReader, baos);
            stamper.setFormFlattening(true);

            stampFields(acroForm, stamper, usenum, verbose);
            baos.flush();
        } finally {
            closeStamper(stamper);
            closeReader(pdfReader);
        }

        return baos.toByteArray();
    }

    @SuppressWarnings("unchecked")
    private void dumpDebugData(PRAcroForm acroForm, String field) {
        PRAcroForm.FieldInformation info = acroForm.getField(field);
        for (PdfName key : (Set<PdfName>) info.getInfo().getKeys()) {
            System.out.println("\t-> " + key + " <-> " + info.getInfo().get(key));
        }
    }

    private void stampFields(PRAcroForm acroForm, PdfStamper stamper, boolean usenum, boolean verbose) {
        final AtomicInteger i = new AtomicInteger();

        AcroFields fields = stamper.getAcroFields();
        @SuppressWarnings("unchecked") Set<String> set = fields.getFields().keySet();
        set.forEach(key -> {
            try {
                String val = usenum ? i.intValue() + "" : key;

                System.out.println(format("Stamping key %s (%s) %s", key, getType(fields, key), usenum ? " as " + val : ""));
                if (verbose) {
                    dumpDebugData(acroForm, key);
                }
                fields.setField(key, val);
                i.incrementAndGet();
            } catch (IOException | DocumentException e) {
                System.err.println(format("Cannot stamp field %s (%s)", key, e.getMessage()));
            }
        });
    }

    private String getType(AcroFields fields, String key) {
        String type;
        switch (fields.getFieldType(key)) {
            case FIELD_TYPE_NONE:
                type = "none";
                break;
            case FIELD_TYPE_PUSHBUTTON:
                type = "pushbutton";
                break;
            case FIELD_TYPE_CHECKBOX:
                type = "checkbox";
                break;
            case FIELD_TYPE_RADIOBUTTON:
                type = "radio";
                break;
            case FIELD_TYPE_TEXT:
                type = "text";
                break;
            case FIELD_TYPE_LIST:
                type = "list";
                break;
            case FIELD_TYPE_COMBO:
                type = "combo";
                break;
            case FIELD_TYPE_SIGNATURE:
                type = "signature";
                break;
            default:
                type = "unknown";
        }
        return type;
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
                System.err.println(format("Cannot release stamper (%s)", e.getMessage()));
            }
        }
    }

    Path printPreFilledPdf(boolean usenum, boolean verbose) throws IOException, DocumentException {
        byte[] doc = prefill(usenum, verbose);

        Path out = createTempFile("stamped", ".pdf");
        write(out, doc, WRITE);
        System.out.println(format("Result is here: %s", out.toAbsolutePath()));

        return out.toAbsolutePath();
    }

}
