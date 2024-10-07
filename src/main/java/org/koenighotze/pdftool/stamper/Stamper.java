package org.koenighotze.pdftool.stamper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import static org.apache.pdfbox.Loader.loadPDF;

public class Stamper {
    private static final Logger LOGGER = LogManager.getLogger(Stamper.class);

    public byte[] prefill(byte[] pdfDocument) throws IOException {
        try (var document = loadPDF(pdfDocument)) {
            if (!isStampable(document)) {
                throw new IOException("Document is not stampable! It does not contain a form!");
            }

            LOGGER.debug("Document has {} pages", document.getNumberOfPages());

            var numberOfStampedFields = stampFields(document.getDocumentCatalog().getAcroForm());
            LOGGER.info("Stamped {} fields", numberOfStampedFields);

            document.getDocumentCatalog().getAcroForm().flatten();

            var baos = new ByteArrayOutputStream();
            document.save(baos);
            return baos.toByteArray();
        }
    }

    private boolean isStampable(PDDocument document) {
        return document.getDocumentCatalog().getAcroForm() != null;
    }

    private int stampFields(PDAcroForm form) {
        return form.getFields().stream().map(PDFieldFacade::new).reduce(0, (pos, field) -> stampField(field, pos), Integer::sum);
    }

    private int stampField(PDFieldFacade field, int fieldNumber) {
        field.stamp();
        return fieldNumber + 1;
    }
}
