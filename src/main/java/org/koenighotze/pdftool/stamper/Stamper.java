package org.koenighotze.pdftool.stamper;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static java.nio.file.Files.createTempFile;
import static java.nio.file.Files.write;
import static java.nio.file.StandardOpenOption.WRITE;
import static org.koenighotze.pdftool.stamper.StampDebugger.dumpDebugData;

public class Stamper {
    private byte[] prefill(boolean useNumber, boolean verbose, Path pdfDocument) throws IOException {
        PDDocument document = null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            document = Loader.loadPDF(pdfDocument.toFile());
            PDAcroForm acroForm = document.getDocumentCatalog().getAcroForm();
            if (verbose) {
                System.out.printf("PDF has %s pages%n", document.getNumberOfPages());
            }

            System.out.println(stampFields(document, useNumber, verbose));

            if (acroForm != null) {
                acroForm.flatten();
            }

            document.save(baos);
        } finally {
            if (document != null) {
                document.close();
            }
        }

        return baos.toByteArray();
    }

    private int stampFields(PDDocument document, boolean useNumbers, boolean verbose) {
        PDAcroForm acroForm = document.getDocumentCatalog().getAcroForm();
        List<PDField> fields = acroForm.getFields();

        return fields.stream().reduce(0, (pos, field) -> stampField(acroForm, useNumbers, verbose, pos, field), Integer::sum);
    }

    private int stampField(PDAcroForm acroForm, boolean useNumbers, boolean verbose, int fieldNumber, PDField field) {
        String fieldIdentifier = valueForType(getType(acroForm, field.getFullyQualifiedName()), useNumbers, fieldNumber, field.getFullyQualifiedName());

        logStamp(useNumbers, field, fieldIdentifier) ;

        if (verbose) {
            dumpDebugData(acroForm, field.getFullyQualifiedName());
        }

        try {
            if (null != fieldIdentifier) {
                field.setValue(fieldIdentifier);
            } else {
                System.err.printf("Field %s not found%n", fieldIdentifier);
            }
        } catch (IOException e) {
            System.err.printf("Cannot stamp field %s (%s)%n", fieldIdentifier, e.getMessage());
        }

        return fieldNumber + 1;
    }

    private void logStamp(boolean useNumbers, PDField field, String val) {
        System.out.printf("Stamping key %s (%s) %s%n", field.getFullyQualifiedName(), field.getFieldType(), useNumbers ? " as " + val : "");
    }

    private String valueForType(String type, boolean useNumbers, int fieldNumber, String key) {
        return switch (type) {
            case "text" -> {
                if (useNumbers) {
                    yield String.valueOf(fieldNumber);
                }
                yield key;
            }
            case "combo" -> "combo";
            case "list" ->
                // TODO get real value
                    null;
            case "checkbox" ->
                // TODO get real value
                    null;
            case "radiobutton" ->
                // TODO get real value
                    null;
            case "pushbutton" ->
                // TODO get real value
                    null;
            case "signature" ->
                // TODO get real value
                    null;
            default -> "unknown";
        };
    }


    private String getType(PDAcroForm acroForm, String key) {
        PDField field = acroForm.getField(key);
        if (field instanceof PDTextField) {
            return "text";
        }
        if (field instanceof PDComboBox) {
            return "combo";
        }
        if (field instanceof PDListBox) {
            return "list";
        }
        if (field instanceof PDCheckBox) {
            return "checkbox";
        }
        if (field instanceof PDRadioButton) {
            return "radiobutton";
        }
        if (field instanceof PDButton) {
            // PDButton is a superclass of PDCheckBox and PDRadioButton, so this check comes last.
            return "pushbutton";
        }
        if (field instanceof PDSignatureField) {
            return "signature";
        }
        return "unknown";
    }

    private void closeDocument(PDDocument document) {
        if (document == null) {
            return;
        }

        try {
            document.close();
        } catch (IOException e) {
            System.err.printf("Cannot release document (%s)%n", e.getMessage());
        }
    }

    public Path printPreFilledPdf(boolean useNumbers, boolean verbose, Path documentPath) throws IOException {
        byte[] doc = prefill(useNumbers, verbose, documentPath);
        String outputDir = System.getenv("OUTPUT_DIR");
        if (outputDir == null) {
            outputDir = System.getProperty("java.io.tmpdir");
        }

        Path out = createTempFile(Path.of(outputDir), "stamped", ".pdf");
        write(out, doc, WRITE);

        return out.toAbsolutePath();

    }

}
