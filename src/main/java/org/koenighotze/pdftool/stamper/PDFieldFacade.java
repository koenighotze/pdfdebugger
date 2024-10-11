package org.koenighotze.pdftool.stamper;

import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDTextField;

import java.io.IOException;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static org.apache.logging.log4j.LogManager.getLogger;

class PDFieldFacade {
    private static final Logger LOGGER = getLogger(PDFieldFacade.class);

    private final PDField field;

    public PDFieldFacade(PDField field) {
        requireNonNull(field);
        this.field = field;
    }

    public void stamp() {
        LOGGER.info("Stamping field with key '{}' of type {}", field.getFullyQualifiedName(), field.getFieldType());

        determineStampValue()
                .ifPresentOrElse(
                        this::setFieldValue,
                        () -> LOGGER.warn("Field with key '{}' cannot be stamped with a value", field.getFullyQualifiedName())
                );
    }

    private void setFieldValue(String value) {
        try {
            field.setValue(value);
        } catch (IOException e) {
            LOGGER.warn("Cannot stamp field with key '{}' because of: {}", field.getFullyQualifiedName(), e.getMessage());
        }
    }

    Optional<String> determineStampValue() {
        if (field instanceof PDTextField) {
            return Optional.of(field.getFullyQualifiedName());
        }
        return Optional.empty();
    }
}
