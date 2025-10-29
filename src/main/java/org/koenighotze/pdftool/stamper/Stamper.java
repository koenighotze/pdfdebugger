package org.koenighotze.pdftool.stamper;

import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;

import java.io.IOException;
import java.io.OutputStream;

import static java.util.Objects.requireNonNull;
import static org.apache.pdfbox.Loader.loadPDF;

public class Stamper {
    public void prefill(byte[] pdfDocument, OutputStream outputStream) throws IOException {
        requireNonNull(pdfDocument);
        requireNonNull(outputStream);
        try (var document = loadPDF(pdfDocument)) {
            var form = document.getDocumentCatalog().getAcroForm();
            if (form == null) {
                throw new IOException("Document is not stampable! It does not contain a form!");
            }
            stampFields(form);

            document.getDocumentCatalog().getAcroForm().flatten();

            document.save(outputStream);
        }
    }

    private void stampFields(PDAcroForm form) {
        form.getFields().stream()
                .map(PDFieldFacade::new)
                .forEach(PDFieldFacade::stamp);
    }
}
