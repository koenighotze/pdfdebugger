package org.koenighotze.pdftool.stamper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;

public class StampDebugger {
    private static final Logger LOGGER = LogManager.getLogger(StampDebugger.class);

    @SuppressWarnings("unchecked")
    static void dumpDebugData(PDAcroForm acroForm, String fieldName) {
        PDField field = acroForm.getField(fieldName);
        if (field != null) {
            LOGGER.info("Field Name: {}", field.getFullyQualifiedName());
            field.getWidgets().forEach(widget -> {
                LOGGER.debug("\t\t-> Widget Annotation Dictionary: {}", widget.getCOSObject());
            });
        } else {
            LOGGER.info("Field {} not found.", fieldName);
        }
    }

    public static void logStamp(String fieldType, String key) {
        LOGGER.info("Stamping key {} of type {}", key, fieldType);
    }
}
