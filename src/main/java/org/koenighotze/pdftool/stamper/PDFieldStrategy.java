package org.koenighotze.pdftool.stamper;

import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;

import java.util.Optional;

public interface PDFieldStrategy {
    default Optional<String> determineStampValueForField(PDField field) {
        return Optional.of(field.getFullyQualifiedName());
    };
}
