package org.koenighotze.pdftool;

import com.lowagie.text.*;
import org.apache.commons.cli.*;

import java.io.*;
import java.nio.file.*;

import static java.lang.String.*;
import static java.nio.file.Files.*;
import static org.apache.commons.cli.Option.*;

/**
 * Simple tool for pre-stamping a PDF form with the keys of the fields as their
 * value.
 *
 * @author dschmitz
 */
public class PdfTool {

    public static void main(String[] args) throws IOException, DocumentException {
        Options options = new Options();
        options.addOption(builder().longOpt("verbose").desc("verbose output").build());
        options.addOption(builder().longOpt("numbers").desc("print numbers instead of names").build());
        options.addOption(builder().longOpt("file").required().argName("PdfDoc").desc("the pdf document").hasArg().build());

        try {
            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(options, args);
            Path path = Paths.get(cmd.getOptionValue("file"));
            if (!exists(path)) {
                System.err.println(format("File %s does not exist!", path.getFileName()));
                return;
            }

            new Stamper(path).printPreFilledPdf(cmd.hasOption("numbers"), cmd.hasOption("verbose"));
        } catch (ParseException e) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("PdfTool", options);
        }

    }

}
