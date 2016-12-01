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
import static javaslang.API.$;
import static javaslang.API.Case;
import static javaslang.API.Match;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;

import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.AcroFields;
import com.lowagie.text.pdf.PRAcroForm;
import com.lowagie.text.pdf.PRAcroForm.FieldInformation;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;
import javaslang.collection.LinkedHashSet;
import javaslang.control.Try;

/**
 * Stamper for acrofields in pdf forms.
 *
 * @author dschmitz
 */
public class Stamper {
    private final Path document;

    Stamper(Path path) {
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
        ((Set<PdfName>) info.getInfo().getKeys())
            .forEach(key -> {
                System.out.println("\t-> " + key + " <-> " + info.getInfo().get(key));
            });
    }

    @SuppressWarnings("unchecked")
    private void stampFields(PRAcroForm acroForm, PdfStamper stamper, boolean usenum, boolean verbose) {
        AcroFields fields = stamper.getAcroFields();
        LinkedHashSet.ofAll((Set<String>) fields.getFields().keySet())
                     .foldLeft(0, (pos, key) -> stampField(acroForm, usenum, verbose, pos, fields, key));
    }

    private int stampField(PRAcroForm acroForm, boolean usenum, boolean verbose, int fieldNumber, AcroFields fields, String key) {
        String fieldIdentifier = usenum ? fieldNumber + "" : key;

        logStamp(usenum, fields, key, fieldIdentifier);

        if (verbose) {
            dumpDebugData(acroForm, key);
        }

        Try.run(() -> fields.setField(key, fieldIdentifier))
           .onFailure(t -> System.err.println(format("Cannot stamp field %s (%s)", key, t.getMessage())));

        return fieldNumber + 1;
    }

    private void logStamp(boolean usenum, AcroFields fields, String key, String val) {
        System.out.println(format("Stamping key %s (%s) %s", key, getType(fields, key), usenum ? " as " + val : ""));
    }

    private String getType(AcroFields fields, String key) {
        return Match(fields.getFieldType(key)).of(
            Case(FIELD_TYPE_NONE, "none"),
            Case(FIELD_TYPE_PUSHBUTTON, "pushbutton"),
            Case(FIELD_TYPE_CHECKBOX, "checkbox"),
            Case(FIELD_TYPE_RADIOBUTTON, "radio"),
            Case(FIELD_TYPE_TEXT, "text"),
            Case(FIELD_TYPE_LIST, "list"),
            Case(FIELD_TYPE_COMBO, "combo"),
            Case(FIELD_TYPE_SIGNATURE, "signature"),
            Case($(), "unknown")
        );
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

        Try.run(stamper::close)
           .onFailure(t -> System.err.println(format("Cannot release stamper (%s)", t.getMessage())));
    }

    Path printPreFilledPdf(boolean usenum, boolean verbose) throws IOException, DocumentException {
        byte[] doc = prefill(usenum, verbose);

        Path out = createTempFile("stamped", ".pdf");
        write(out, doc, WRITE);

        return out.toAbsolutePath();
    }

}
