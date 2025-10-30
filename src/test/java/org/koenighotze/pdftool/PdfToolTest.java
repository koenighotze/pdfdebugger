package org.koenighotze.pdftool;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.util.Objects.requireNonNull;
import static org.apache.pdfbox.Loader.loadPDF;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class PdfToolTest {
    private Path path;

    @BeforeEach
    public void setUp() throws URISyntaxException {
        path = Paths.get(requireNonNull(PdfToolTest.class.getResource("/interactiveform_enabled.pdf"))
                .toURI());
    }

    @Test
    public void the_form_fields_of_a_pdf_are_filled_with_the_fields_name() throws IOException {
        Path result = new PdfTool().printPreFilledPdf(this.path, Path.of(System.getProperty("java.io.tmpdir")));

        PDDocument document = loadPDF(result.toFile());
        String text = new PDFTextStripper().getText(document);
        assertThat(text).contains("Telephone_Work");
    }


    @Test
    public void an_exception_is_thrown_if_the_input_file_does_not_exist() {
        assertThatThrownBy(() -> new PdfTool().printPreFilledPdf(Paths.get("does_not_exist.pdf"), Path.of("target")))
                .isInstanceOf(IOException.class)
                .hasMessage("PDF file does not exist!");
    }
}
