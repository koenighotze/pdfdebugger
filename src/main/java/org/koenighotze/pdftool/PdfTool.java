package org.koenighotze.pdftool;

import com.lowagie.text.*;

import java.io.*;
import java.nio.file.*;

import static java.lang.String.*;
import static java.nio.file.Files.*;

/**
 * Simple tool for pre-stamping a PDF form with the keys of the fields as their
 * value.
 *
 * @author dschmitz
 */
public class PdfTool {

    public static void main(String[] args) throws IOException, DocumentException {
        if (args.length < 1 || args.length > 2) {
            printUsage();

            return;
        }
        boolean usenum = false;
        if (args.length == 2) {
            usenum = "usenum".equals(args[1]);
        }

        Path path = Paths.get(args[0]);
        if (!exists(path)) {
            System.err.println(format("File %s does not exist!", path.getFileName()));
            return;
        }

        new Stamper(path).printPreFilledPdf(usenum);
    }

    private static void printUsage() {
        System.out.printf("Usage: PdfTool <file.pdf> [usenum]\n");
    }
}
