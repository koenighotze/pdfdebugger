package org.koenighotze.pdftool;

import com.lowagie.text.DocumentException;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.koenighotze.pdftool.stamper.Stamper;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.file.Files.exists;
import static org.apache.commons.cli.Option.builder;

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
            var parseConfiguration = ParseConfiguration.fromCliArguments(args, options);

            pdfTool.stampPdfFormToFile(parseConfiguration);
        } catch (ParseException e) {
            pdfTool.printUsage(options);
        }
    }

    private void stampPdfFormToFile(ParseConfiguration parseConfiguration) throws IOException, DocumentException {
        if (exists(parseConfiguration.filename)) {
            var result = new Stamper().printPreFilledPdf(parseConfiguration.numbers, parseConfiguration.verbose, parseConfiguration.filename);
            System.out.printf("Result is here: %s%n", result.toAbsolutePath());
        } else {
            System.err.println("File " + parseConfiguration.filename.toAbsolutePath() + " does not exist!");
        }
    }

    private void printUsage(Options options) {
        new HelpFormatter().printHelp("PdfTool", options);
    }

    public record ParseConfiguration(Path filename, Boolean numbers, Boolean verbose) {
        static ParseConfiguration fromCliArguments(String[] args, Options options) throws ParseException {
            var parsedOptions = new DefaultParser().parse(options, args);
            return new ParseConfiguration(
                    Paths.get(parsedOptions.getOptionValue("file")),
                    parsedOptions.hasOption("numbers"),
                    parsedOptions.hasOption("verbose"));
        }
    }


}
