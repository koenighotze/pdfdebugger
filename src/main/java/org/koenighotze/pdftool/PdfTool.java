package org.koenighotze.pdftool;

import static java.nio.file.Files.exists;
import static org.apache.commons.cli.Option.builder;

import java.io.*;
import java.nio.file.*;

import com.lowagie.text.*;
import org.apache.commons.cli.*;

/**
 * Simple tool for pre-stamping a PDF form with the keys of the fields as their
 * value.
 *
 * @author dschmitz
 */
public class PdfTool {
    private Options buildCliOptions() {
        return new Options().addOption(builder().longOpt("verbose")
                        .desc("verbose output")
                        .build())
                .addOption(builder().longOpt("numbers")
                        .desc("print numbers instead of names")
                        .build())
                .addOption(builder().longOpt("file")
                        .required()
                        .argName("PdfDoc")
                        .desc("the pdf document")
                        .hasArg()
                        .build());
    }


    public static void main(String[] args) throws IOException, DocumentException {
        var pdfTool = new PdfTool();
        Options options = pdfTool.buildCliOptions();

        try {
            var parseConfiguration = pdfTool.parseCliArguments(args, options);

            if (exists(parseConfiguration.filename)) {
                var result = stampPdfTemplate(parseConfiguration);
                System.out.printf("Result is here: %s%n", result.toAbsolutePath());
            }
            else {
                System.err.println("File " + parseConfiguration.filename.toAbsolutePath() + " does not exist!");
            }

        } catch (ParseException e) {
            printUsage(options);
        }
    }

    private ParseConfiguration parseCliArguments(String[] args, Options options) throws ParseException {
        var parsedOptions = new DefaultParser().parse(options, args);
        return extractOptions(parsedOptions);
    }

    private static Path stampPdfTemplate(ParseConfiguration parseConfiguration) throws DocumentException, IOException {
        return new Stamper(parseConfiguration.filename).printPreFilledPdf(parseConfiguration.numbers, parseConfiguration.verbose);
    }

    private static void printUsage(Options options) {
        new HelpFormatter().printHelp("PdfTool", options);
    }

    public record ParseConfiguration(Path filename, Boolean numbers, Boolean verbose) { }

    private static ParseConfiguration extractOptions(CommandLine cmd) {
        return new ParseConfiguration(Paths.get(cmd.getOptionValue("file")), cmd.hasOption("numbers"), cmd.hasOption("verbose"));
    }

}
