package org.koenighotze.pdftool.stamper;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.apache.pdfbox.Loader.loadPDF;

public class Stamper {
    public byte[] prefill(byte[] pdfDocument) throws IOException {
        try (var document = loadPDF(pdfDocument)) {
            var form = document.getDocumentCatalog().getAcroForm();
            if (form == null) {
                throw new IOException("Document is not stampable! It does not contain a form!");
            }
            stampFields(form);

            document.getDocumentCatalog().getAcroForm().flatten();

            return saveDocument(document);
        }
    }

    private byte[] saveDocument(PDDocument document) throws IOException {
        try (var baos = new ByteArrayOutputStream()) {
            document.save(baos);
            return baos.toByteArray();
        }
    }

    private void stampFields(PDAcroForm form) {
        form.getFields().stream()
                .map(PDFieldFacade::new)
                .forEach(PDFieldFacade::stamp);
    }
}
