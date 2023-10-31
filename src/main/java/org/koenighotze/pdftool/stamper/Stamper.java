package org.koenighotze.pdftool.stamper;

import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.*;
import com.lowagie.text.pdf.PRAcroForm.FieldInformation;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;

import static com.lowagie.text.pdf.AcroFields.*;
import static java.nio.file.Files.*;
import static java.nio.file.StandardOpenOption.WRITE;
import static java.util.Objects.requireNonNull;

/**
 * Stamper for acrofields in pdf forms.
 *
 * @author dschmitz
 */
public class Stamper {
    private byte[] prefill(boolean useNumber, boolean verbose, Path pdfDocument) throws IOException, DocumentException {
        PdfReader pdfReader = null;
        PdfStamper stamper = null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            pdfReader = new PdfReader(newInputStream(pdfDocument));
            PRAcroForm acroForm = pdfReader.getAcroForm();
            System.out.printf("PDF is in Version %s and has %s pages%n", pdfReader.getPdfVersion(),
                                      pdfReader.getNumberOfPages());

            stamper = new PdfStamper(pdfReader, baos);
            stamper.setFormFlattening(true);

            System.out.println(stampFields(acroForm, stamper, useNumber, verbose));
        } finally {
            closeStamper(stamper);
            closeReader(pdfReader);
        }
        baos.flush();
        return baos.toByteArray();
    }

    @SuppressWarnings("unchecked")
    private void dumpDebugData(PRAcroForm acroForm, String field) {
        FieldInformation info = acroForm.getField(field);
        ((Set<PdfName>) info.getInfo()
                            .getKeys()).forEach(key -> {
            System.out.println("\t-> " + key + " <-> " + info.getInfo()
                                                             .get(key));
        });
    }

    @SuppressWarnings("unchecked")
    private int stampFields(PRAcroForm acroForm, PdfStamper stamper, boolean useNumbers, boolean verbose) {
        AcroFields fields = stamper.getAcroFields();

        var fieldKeys = (Set<String>) fields.getFields().keySet();
        return fieldKeys.stream().reduce(0, (pos, key) -> stampField(acroForm, useNumbers, verbose, pos, fields, key), Integer::sum);
    }

    private int stampField(PRAcroForm acroForm, boolean useNumbers, boolean verbose, int fieldNumber, AcroFields fields, String key) {
        String fieldIdentifier = useNumbers ? fieldNumber + "" : key;

        logStamp(useNumbers, fields, key, fieldIdentifier);

        if (verbose) {
            dumpDebugData(acroForm, key);
        }

        try {
            fields.setField(key, fieldIdentifier);
        } catch (IOException | DocumentException e) {
            System.err.printf("Cannot stamp field %s (%s)%n", key, e.getMessage());
        }

        return fieldNumber + 1;
    }

    private void logStamp(boolean useNumbers, AcroFields fields, String key, String val) {
        System.out.printf("Stamping key %s (%s) %s%n", key, getType(fields, key), useNumbers ? " as " + val : "");
    }

    private String getType(AcroFields fields, String key) {
        return switch (fields.getFieldType(key)) {
            case FIELD_TYPE_NONE -> "none";
            case FIELD_TYPE_PUSHBUTTON -> "pushbutton";
            case FIELD_TYPE_CHECKBOX -> "checkbox";
            case FIELD_TYPE_RADIOBUTTON -> "radio";
            case FIELD_TYPE_TEXT, FIELD_TYPE_LIST -> "list";
            case FIELD_TYPE_COMBO -> "combo";
            case FIELD_TYPE_SIGNATURE -> "signature";

            default -> "unknown";
        };
    }

    private void closeReader(PdfReader pdfReader) {
        if (pdfReader != null) {
            pdfReader.close();
        }
    }

    private void closeStamper(PdfStamper stamper) {
        if (stamper == null) {
            return;
        }

        try {
            stamper.close();
        } catch (IOException | DocumentException e) {
            System.err.printf("Cannot release stamper (%s)%n", e.getMessage());
        }
    }

    public Path printPreFilledPdf(boolean useNumbers, boolean verbose, Path document) throws IOException, DocumentException {
        byte[] doc = prefill(useNumbers, verbose, document);
        String outputDir = System.getenv("OUTPUT_DIR");
        if (outputDir == null) {
            outputDir = System.getProperty("java.io.tmpdir");
        }

        Path out = createTempFile(Path.of(outputDir), "stamped", ".pdf");
        write(out, doc, WRITE);

        return out.toAbsolutePath();
    }

}
