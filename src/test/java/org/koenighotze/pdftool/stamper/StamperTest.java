package org.koenighotze.pdftool.stamper;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;
import static org.apache.pdfbox.Loader.loadPDF;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author dschmitz
 */
public class StamperTest {
    private static final String KNOWN_FIELD_NAME = "Name_Last";
    private Path path;
    private PrintStream originalOut;
    private ByteArrayOutputStream stdOutBos;

    @BeforeEach
    public void setUp() throws URISyntaxException {
        path = Paths.get(requireNonNull(StamperTest.class.getResource("/interactiveform_enabled.pdf"))
                .toURI());

        originalOut = System.out;
        stdOutBos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(new BufferedOutputStream(stdOutBos), true));
    }

    @AfterEach
    public void tearDown() {
        if (null != originalOut) {
            System.setOut(originalOut);
        }
    }

    @Test
    public void stamping_a_form_returns_the_result_as_a_path() throws IOException {
        Path result = new Stamper().printPreFilledPdf(false, false, this.path);

        assertThat(Files.exists(result)).isTrue();
    }

    @Test
    public void the_form_fields_of_a_pdf_are_filled_with_the_fields_name() throws IOException {
        Path result = new Stamper().printPreFilledPdf(false, true, this.path);

        PDDocument document = loadPDF(result.toFile());
        String text = new PDFTextStripper().getText(document);

        assertThat(text).contains("Telephone_Work");
    }

    @Test
    public void the_form_fields_of_a_pdf_are_filled_with_consecutive_numbers_if_the_usename_flag_is_used() throws IOException {
        Path result = new Stamper().printPreFilledPdf(true, false, this.path);

        PDDocument document = loadPDF(result.toFile());
        String text = new PDFTextStripper().getText(document);

        assertThat(text).contains("12"); // cannot check all fields, as checkboxes behave differently
    }

    @Test
    public void the_field_information_is_printed_to_stdout() throws IOException {
        new Stamper().printPreFilledPdf(true, false, this.path);

        assertThat(stdOutBos.toString(UTF_8)).contains("Stamping key " + KNOWN_FIELD_NAME);
    }

}
