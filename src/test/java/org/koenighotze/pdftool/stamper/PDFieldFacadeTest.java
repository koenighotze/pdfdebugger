// src/test/java/org/koenighotze/pdftool/stamper/PDFieldFacadeTest.java
package org.koenighotze.pdftool.stamper;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDRadioButton;
import org.apache.pdfbox.pdmodel.interactive.form.PDSignatureField;
import org.apache.pdfbox.pdmodel.interactive.form.PDTextField;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class PDFieldFacadeTest {
    private PDTextField field;

    @BeforeEach
    public void setup() {
        var form = new PDAcroForm(new PDDocument());
        form.setDefaultResources(new PDResources());
        field = new PDTextField(form);
        field.setDefaultAppearance("foo");
        field.setPartialName("testField");
    }

    @Test
    public void when_determining_the_stamp_value_for_a_text_field_the_fields_name_should_be_returned() {
        var result = new PDFieldFacade(field).determineStampValue();

        assertThat(result.get()).isEqualTo("testField");
    }

    @Test
    public void when_determining_the_stamp_value_for_a_non_text_field_the_fields_name_should_be_empty() {
        var field = new PDRadioButton(new PDAcroForm(new PDDocument()));
        field.setPartialName("radioButton");

        var result = new PDFieldFacade(field).determineStampValue();

        assertThat(result).isEmpty();
    }

    @Test
    public void when_stamping_a_textfield_the_name_of_the_field_should_be_stamped() {
        new PDFieldFacade(field).stamp();

        assertThat(field.getValue()).isEqualTo("testField");
    }

    @Test
    public void when_stamping_a_textfield_and_the_value_is_empty_nothing_is_stamped() {
        var field = new PDSignatureField(new PDAcroForm(new PDDocument()));
        field.setPartialName("signature");

        new PDFieldFacade(field).stamp();

        assertThat(field.getValue()).isNull();
    }

    @Test
    public void when_stamping_a_textfield_and_an_exception_is_thrown_the_exception_is_logged() throws IOException {
        var field = mock(PDTextField.class);
        when(field.getFullyQualifiedName()).thenReturn("testField");
        doThrow(new IOException("Test")).when(field).setValue(anyString());

        new PDFieldFacade(field).stamp();
        
        verify(field).setValue("testField");
    }
}