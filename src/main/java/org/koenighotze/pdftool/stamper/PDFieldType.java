package org.koenighotze.pdftool.stamper;

import org.apache.pdfbox.pdmodel.interactive.form.*;

public enum PDFieldType {
    TEXT,
    COMBO(new PDFieldStrategy() {}),
    LIST(new PDFieldStrategy() {}),
    CHECKBOX(new PDFieldStrategy() {}),
    RADIOBUTTON(new PDFieldStrategy() {}),
    PUSHBUTTON(new PDFieldStrategy() {}),
    SIGNATURE(new PDFieldStrategy() {}),
    UNKNOWN;

    private final PDFieldStrategy pdFieldStrategy;

    PDFieldType(PDFieldStrategy pdFieldStrategy) {
        this.pdFieldStrategy = pdFieldStrategy;
    }

    PDFieldType() {
        this(new PDFieldStrategy() {});
    }

    public static PDFieldType fromPDField(PDField field) {
        if (field instanceof PDTextField) {
            return TEXT;
        }
        if (field instanceof PDComboBox) {
            return COMBO;
        }
        if (field instanceof PDListBox) {
            return LIST;
        }
        if (field instanceof PDCheckBox) {
            return CHECKBOX;
        }
        if (field instanceof PDRadioButton) {
            return RADIOBUTTON;
        }
        if (field instanceof PDButton) {
            return PUSHBUTTON;
        }
        if (field instanceof PDSignatureField) {
            return SIGNATURE;
        }
        return UNKNOWN;
    }

    public PDFieldStrategy getPdFieldStrategy() {
        return pdFieldStrategy;
    }
}
