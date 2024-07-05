package org.koenighotze.pdftool.stamper;

import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;

public class StampDebugger {
    @SuppressWarnings("unchecked")
    static void dumpDebugData(PDAcroForm acroForm, String fieldName) {
        PDField field = acroForm.getField(fieldName);
        if (field != null) {
            System.out.println("\t-> Field Name: " + field.getFullyQualifiedName());
            field.getWidgets().forEach(widget -> {
                System.out.println("\t\t-> Widget Annotation Dictionary: " + widget.getCOSObject());
            });
        } else {
            System.out.println("Field " + fieldName + " not found.");
        }
    }
}
