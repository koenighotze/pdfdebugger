package org.koenighotze.pdftool;

import static java.lang.System.setErr;
import static java.lang.System.setOut;
import static java.nio.charset.StandardCharsets.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.koenighotze.pdftool.PdfTool.main;

import java.io.*;
import java.nio.charset.StandardCharsets;

import com.lowagie.text.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;

/**
 * @author dschmitz
 */
public class PdfToolTest {

    private PrintStream originalOut;
    private ByteArrayOutputStream stdOutBos;
    private PrintStream originalErr;
    private ByteArrayOutputStream stdErrBos;

    @BeforeEach
    public void setUp() throws IOException, DocumentException {
        originalOut = System.out;
        stdOutBos = new ByteArrayOutputStream();
        setOut(new PrintStream(new BufferedOutputStream(stdOutBos), true));

        originalErr = System.err;
        stdErrBos = new ByteArrayOutputStream();
        setErr(new PrintStream(new BufferedOutputStream(stdErrBos), true));
    }

    @AfterEach
    public void tearDown() {
        if (null != originalOut) {
            setOut(originalOut);
        }

        if (null != originalErr) {
            setErr(originalErr);
        }
    }

    @Test
    public void calling_the_main_method_without_args_prints_the_usage() throws IOException, DocumentException {
        main(new String[]{});

        assertThat(stdOutBos.toString("UTF-8")).contains("usage");
    }

    @Test
    public void calling_the_main_method_with_more_than_2_args_prints_the_usage() throws IOException, DocumentException {
        main(new String[]{"bla", "blub", "tuk"});

        assertThat(stdOutBos.toString(UTF_8)).contains("usage");
    }

    @Test
    public void if_the_pdf_does_not_exist_no_exception_is_thrown() throws IOException, DocumentException {
        main(new String[]{"--file", "blafasel"});
        System.out.println(stdOutBos.toString(UTF_8));
        assertThat(stdErrBos.toString(UTF_8)).contains("does not exist!");
    }

}
