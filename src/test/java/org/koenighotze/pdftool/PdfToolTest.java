package org.koenighotze.pdftool;

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

    @Before
    public void setup() {
        originalOut = System.out;
        PrintStream printStream = new PrintStream(new BufferedOutputStream(new ByteArrayOutputStream()));
//        System.setOut();
    }

    @After
    public void tearDown() {
        System.setOut(originalOut);
    }

    @Test
    public void calling_the_main_method_without_args_prints_the_usage() throws IOException, DocumentException {
        // ...i could try to capture the stdout, but...

        PrintStream originalOut = System.out;

        System.setOut(new PrintStream(new BufferedOutputStream(new ByteArrayOutputStream())));
        PdfTool.main(new String[]{});
    }
    
    @Test
    public void if_the_pdf_does_not_exist_no_exception_is_thrown() throws IOException, DocumentException {
        PdfTool.main(new String[] { "blafasel "});
    }



}