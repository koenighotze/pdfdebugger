package org.koenighotze.pdftool.stamper;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDTextField;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StamperTest {
    private Stamper stamper;
    private byte[] validPdf;
    private ByteArrayOutputStream outputStream;

    @BeforeEach
    public void setUp() throws IOException {
        stamper = new Stamper();
        validPdf = createValidPdf(); // Method to create a valid PDF as byte[]
        outputStream = new ByteArrayOutputStream();
    }

    private PDAnnotationWidget createTextFieldWidget(PDPage page) {
        PDAnnotationWidget widget = new PDAnnotationWidget();
        PDRectangle rect = new PDRectangle(50, 700, 200, 50);
        widget.setRectangle(rect);
        widget.setPage(page);
        return widget;
    }

    private byte[] createValidPdf() throws IOException {
        try (var document = new PDDocument()) {
            var page = new PDPage();
            document.addPage(page);

            var acroForm = new PDAcroForm(document);

            // Set up default resources with font
            PDResources resources = new PDResources();
            resources.put(org.apache.pdfbox.cos.COSName.getPDFName("Helv"), new PDType1Font(Standard14Fonts.FontName.HELVETICA));
            acroForm.setDefaultResources(resources);
            acroForm.setDefaultAppearance("/Helv 0 Tf 0 g");

            document.getDocumentCatalog().setAcroForm(acroForm);

            PDTextField textField = new PDTextField(acroForm);
            textField.setPartialName("SampleTextField");
            textField.setValue("Default Value");
            textField.setRequired(true);
            textField.setDefaultAppearance("/Helv 0 Tf 0 g");
            textField.setWidgets(Collections.singletonList(createTextFieldWidget(page)));

            acroForm.getFields().add(textField);

            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                document.save(outputStream);
                return outputStream.toByteArray();
            }
        }
    }

    @Test
    public void testPrefill_withValidPdf_shouldStampSuccessfully() throws IOException {
        stamper.prefill(validPdf, outputStream);

        // Assert that the output stream has content (indicating the PDF was processed)
        assertTrue(outputStream.size() > 0);
    }

    @Test
    public void testPrefill_withNullPdfDocument_shouldThrowException() {
        assertThrows(NullPointerException.class, () -> {
            stamper.prefill(null, outputStream);
        });
    }

    @Test
    public void testPrefill_withNullOutputStream_shouldThrowException() {
        assertThrows(NullPointerException.class, () -> {
            stamper.prefill(validPdf, null);
        });
    }

    @Test
    public void testPrefill_withPdfWithoutAcroForm_shouldThrowIOException() throws IOException {
        // Create a real PDF without an AcroForm
        byte[] pdfWithoutAcroForm = createPdfWithoutAcroForm();

        assertThrows(IOException.class, () -> {
            stamper.prefill(pdfWithoutAcroForm, outputStream);
        });
    }

    private byte[] createPdfWithoutAcroForm() throws IOException {
        // Create a simple PDF without an AcroForm
        try (var document = new PDDocument()) {
            var page = new PDPage();
            document.addPage(page);

            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                document.save(outputStream);
                return outputStream.toByteArray();
            }
        }
    }
}