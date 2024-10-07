package org.koenighotze.pdftool.stamper;

import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;

import java.io.IOException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.util.logging.Level.WARNING;
import static org.koenighotze.pdftool.stamper.PDFieldStrategyFactory.forField;
import static org.koenighotze.pdftool.stamper.StampDebugger.dumpDebugData;

public class PDFieldFacade {
    private static final Logger LOGGER = Logger.getLogger( PDFieldFacade.class.getName() );

    private final PDField field;

    public PDFieldFacade(PDField field) {
        assert null != field;

        this.field = field;
    }

    private void logStamp(boolean useNumbers, String fieldType, String key, String val) {
        System.out.printf("Stamping key %s (%s) %s%n", key, fieldType, useNumbers ? " as " + val : "");
    }

    public void stamp(boolean useNumbers, boolean verbose, int fieldNumber) {
        final Optional<String> stampValue = forField(field).determineStampValueForField(field);

        logStamp(useNumbers, field.getFieldType(), field.getPartialName(), stampValue.orElse("n/a"));

        try {
            if (stampValue.isPresent()) {
                field.setValue(stampValue.get());
            } else {
                LOGGER.log(WARNING, "Field {0} cannot be stamped with a value", field.getFullyQualifiedName());
            }
        } catch (IOException e) {
            LOGGER.log(WARNING, "Cannot stamp field {0} ({1})", new Object[] { field.getFullyQualifiedName(), e.getMessage() });
        }

        if (verbose) {
            dumpDebugData(field.getAcroForm(), field.getFullyQualifiedName());
        }
    }
}
