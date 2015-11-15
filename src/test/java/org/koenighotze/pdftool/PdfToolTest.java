package org.koenighotze.pdftool;

import com.lowagie.text.*;
import org.junit.*;

import java.io.*;

import static org.fest.assertions.Assertions.*;
import static org.koenighotze.pdftool.PdfTool.*;

/**
 * @author dschmitz
 */
public class PdfToolTest {

    private PrintStream originalOut;
    private ByteArrayOutputStream stdOutBos;
    private PrintStream originalErr;
    private ByteArrayOutputStream stdErrBos;

    @Before
    public void setup() {
        originalOut = System.out;
        stdOutBos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(new BufferedOutputStream(stdOutBos), true));

        originalErr = System.err;
        stdErrBos = new ByteArrayOutputStream();
        System.setErr(new PrintStream(new BufferedOutputStream(stdErrBos), true));
    }

    @After
    public void tearDown() {
        if (null != originalOut) {
            System.setOut(originalOut);
        }

        if (null != originalErr) {
            System.setErr(originalErr);
        }
    }

    @Test
    public void calling_the_main_method_without_args_prints_the_usage() throws IOException, DocumentException {
        main(new String[] {});

        assertThat(stdOutBos.toString("UTF-8")).contains("Usage");
    }

    @Test
    public void calling_the_main_method_with_more_than_2_args_prints_the_usage() throws IOException, DocumentException {
        main(new String[] { "bla", "blub", "tuk" });

        assertThat(stdOutBos.toString("UTF-8")).contains("Usage");
    }

    @Test
    public void if_the_pdf_does_not_exist_no_exception_is_thrown() throws IOException, DocumentException {
        main(new String[] { "blafasel" });
        assertThat(stdErrBos.toString("UTF-8")).contains("blafasel does not exist");
    }

}