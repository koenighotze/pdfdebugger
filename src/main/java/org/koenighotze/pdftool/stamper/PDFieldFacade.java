package org.koenighotze.pdftool.stamper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.pdmodel.interactive.form.*;

import java.io.IOException;
import java.util.Optional;

import static org.apache.logging.log4j.LogManager.getLogger;

public class PDFieldFacade {
    private static final Logger LOGGER = getLogger(PDFieldFacade.class);

    private final PDField field;

    public PDFieldFacade(PDField field) {
        assert null != field;

        this.field = field;
    }

    public void stamp() {
        LOGGER.info("Stamping field with key '{}' of type {}", field.getFullyQualifiedName(), field.getFieldType());
        final Optional<String> stampValue = determineStampValue();

        if (stampValue.isEmpty()) {
            LOGGER.warn("Field with key '{}' cannot be stamped with a value", field.getFullyQualifiedName());
            return;
        }

        try {
            field.setValue(stampValue.get());
        } catch (IOException e) {
            LOGGER.warn("Cannot stamp field with key {} because of: {}", field.getFullyQualifiedName(), e.getMessage());
        }
    }

    private Optional<String> determineStampValue() {
        if (field instanceof PDTextField) {
            return Optional.of(field.getFullyQualifiedName());
        }
        return Optional.empty();
    }
}
