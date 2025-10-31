package org.koenighotze.pdftool;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;

import static java.lang.System.setOut;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author dschmitz
 */
public class PdfToolCliTest {
    private PrintStream originalOut;
    private ByteArrayOutputStream stdOutBos;

    @BeforeEach
    public void setUp() throws URISyntaxException {
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
    public void if_the_pdf_does_not_exist_exception_is_thrown() throws IOException {
        assertThatThrownBy(() -> PdfToolCli.main(new String[]{"--file", "blafasel"}))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("does not exist!");
    }
}
