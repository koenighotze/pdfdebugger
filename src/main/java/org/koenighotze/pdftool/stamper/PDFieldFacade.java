package org.koenighotze.pdftool.stamper;

import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;

import java.io.IOException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.util.logging.Level.WARNING;
import static org.koenighotze.pdftool.stamper.PDFieldStrategyFactory.forField;

public class PDFieldFacade {
    private static final Logger LOGGER = Logger.getLogger( PDFieldFacade.class.getName() );

    private final PDField field;

    public PDFieldFacade(PDField field) {
        assert null != field;

        this.field = field;
    }

    public void stamp(boolean useNumbers, boolean verbose, int fieldNumber) {
        final Optional<String> stampValue = forField(field).determineStampValueForField(field);

//        logStamp(useNumbers, field, fieldIdentifier) ;

//        if (verbose) {
//        LOGGER.log( Level.FINE, "processing {0} entries in loop", list.size() );

//            dumpDebugData(acroForm, field.getFullyQualifiedName());
//        }

        try {
            stampValue.map(field::setValue).orElseThrow(() -> new IOException("Cannot stamp field")

            if (null != stampValue) {
                field.setValue(stampValue);
            } else {
                LOGGER.log(WARNING, "Field {0} cannot be stamped with a value", field.getFullyQualifiedName());
            }
        } catch (IOException e) {
            LOGGER.log(WARNING, "Cannot stamp field {0} ({1})", new Object[] { field.getFullyQualifiedName(), e.getMessage() });
        }

//        return fieldNumber + 1;
    }
}
