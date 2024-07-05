package org.koenighotze.pdftool.stamper;

import org.apache.pdfbox.pdmodel.interactive.form.PDField;

import static org.koenighotze.pdftool.stamper.PDFieldType.fromPDField;

public class PDFieldStrategyFactory {
    public static PDFieldStrategy forField(PDField field) {
        assert null != field;

        final PDFieldType pdFieldType = fromPDField(field);
        return pdFieldType.getPdFieldStrategy();
    }
}
