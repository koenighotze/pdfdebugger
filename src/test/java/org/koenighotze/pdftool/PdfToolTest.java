package org.koenighotze.pdftool;

import static java.lang.System.setErr;
import static java.lang.System.setOut;
import static org.fest.assertions.Assertions.assertThat;
import static org.koenighotze.pdftool.PdfTool.main;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import com.lowagie.text.DocumentException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author dschmitz
 */
public class PdfToolTest {

    private PrintStream originalOut;
    private ByteArrayOutputStream stdOutBos;
    private PrintStream originalErr;
    private ByteArrayOutputStream stdErrBos;

    @Before
    public void setUp() throws IOException, DocumentException {
        originalOut = System.out;
        stdOutBos = new ByteArrayOutputStream();
        setOut(new PrintStream(new BufferedOutputStream(stdOutBos), true));

        originalErr = System.err;
        stdErrBos = new ByteArrayOutputStream();
        setErr(new PrintStream(new BufferedOutputStream(stdErrBos), true));
    }

    @After
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

        assertThat(stdOutBos.toString("UTF-8")).contains("usage");
    }

    @Test
    public void if_the_pdf_does_not_exist_no_exception_is_thrown() throws IOException, DocumentException {
        main(new String[]{"--file", "blafasel"});
        System.out.println(stdOutBos.toString("UTF-8"));
        assertThat(stdErrBos.toString("UTF-8")).contains("File does not exist!");
    }

}