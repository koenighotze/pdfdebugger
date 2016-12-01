package org.koenighotze.pdftool;

import static org.fest.assertions.Assertions.assertThat;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.lowagie.text.DocumentException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author dschmitz
 */
public class StamperTest {
    private static final String KNOWN_FIELD_NAME = "Name_Last";
    private Path path;
    private PrintStream originalOut;
    private ByteArrayOutputStream stdOutBos;

    @Before
    public void setUp() throws URISyntaxException {
        path = Paths.get(StamperTest.class.getResource("interactiveform_enabled.pdf").toURI());

        originalOut = System.out;
        stdOutBos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(new BufferedOutputStream(stdOutBos), true));
    }

    @After
    public void tearDown() {
        if (null != originalOut) {
            System.setOut(originalOut);
        }
    }

    @Test
    public void stamping_a_form_returns_the_result_as_a_path() throws IOException, DocumentException {
        Path result = new Stamper(this.path).printPreFilledPdf(false, false);

        assertThat(Files.exists(result)).isTrue();
    }

    @Test
    public void the_form_fields_of_a_pdf_are_filled_with_the_fields_name() throws URISyntaxException, IOException, DocumentException {
        Path result = new Stamper(this.path).printPreFilledPdf(false, false);

        PDDocument document = PDDocument.load(result.toFile());
        String text = new PDFTextStripper().getText(document);

        assertThat(text).contains("Telephone_Work");
    }


    @Test
    public void the_form_fields_of_a_pdf_are_filled_with_consecutive_numbers_if_the_usename_flag_is_used() throws IOException, DocumentException {
        Path result = new Stamper(this.path).printPreFilledPdf(true, false);

        PDDocument document = PDDocument.load(result.toFile());
        String text = new PDFTextStripper().getText(document);

        assertThat(text).contains("12"); // cannot check all fields, as checkboxes behave differently
    }

    @Test
    public void the_field_information_is_printed_to_stdout() throws IOException, DocumentException {
        new Stamper(this.path).printPreFilledPdf(true, false);

        assertThat(stdOutBos.toString("UTF-8")).contains("Stamping key " + KNOWN_FIELD_NAME);
    }

}