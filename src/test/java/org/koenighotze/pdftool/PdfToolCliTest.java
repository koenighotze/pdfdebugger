package org.koenighotze.pdftool;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.koenighotze.pdftool.stamper.StamperTest;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.lang.System.setOut;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;
import static org.apache.pdfbox.Loader.loadPDF;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author dschmitz
 */
public class PdfToolCliTest {
    private PrintStream originalOut;
    private ByteArrayOutputStream stdOutBos;
    private Path path;

    @BeforeEach
    public void setUp() throws URISyntaxException {
        path = Paths.get(requireNonNull(StamperTest.class.getResource("/interactiveform_enabled.pdf"))
                .toURI());

        originalOut = System.out;
        stdOutBos = new ByteArrayOutputStream();
        setOut(new PrintStream(new BufferedOutputStream(stdOutBos), true));
    }

    @AfterEach
    public void tearDown() {
        if (null != originalOut) {
            setOut(originalOut);
        }
    }

    @Test
    public void the_form_fields_of_a_pdf_are_filled_with_the_fields_name() throws IOException {
        Path result = new PdfToolCli().printPreFilledPdf(this.path, Path.of(System.getProperty("java.io.tmpdir")));

        PDDocument document = loadPDF(result.toFile());
        String text = new PDFTextStripper().getText(document);

        assertThat(text).contains("Telephone_Work");
    }

    @Test
    public void calling_the_main_method_without_args_prints_the_usage() throws IOException {
        PdfToolCli.main(new String[]{});

        assertThat(stdOutBos.toString(UTF_8)).contains("usage");
    }

    @Test
    public void calling_the_main_method_with_more_than_2_args_prints_the_usage() throws IOException {
        PdfToolCli.main(new String[]{"bla", "blub", "tuk"});

        assertThat(stdOutBos.toString(UTF_8)).contains("usage");
    }

    @Test
    public void if_the_pdf_does_not_exist_no_exception_is_thrown() throws IOException {
        PdfToolCli.main(new String[]{"--file", "blafasel"});
        // TODO: Test that exception is logged
        //        System.out.println(stdOutBos.toString(UTF_8));
        //        assertThat(stdErrBos.toString(UTF_8)).contains("does not exist!");
    }
}
