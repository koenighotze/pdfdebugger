package org.koenighotze.pdftool;

import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.koenighotze.pdftool.stamper.Stamper;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.file.Files.*;
import static java.nio.file.Files.exists;
import static java.nio.file.StandardOpenOption.WRITE;
import static org.apache.commons.cli.Option.builder;

public class PdfToolCli {
    public record ParseConfiguration(Path filename, Boolean numbers, Boolean verbose) {
        static ParseConfiguration fromCliArguments(String[] args, Options options) throws ParseException {
            var parsedOptions = new DefaultParser().parse(options, args);
            return new ParseConfiguration(
                    Paths.get(parsedOptions.getOptionValue("file")),
                    parsedOptions.hasOption("numbers"),
                    parsedOptions.hasOption("verbose"));
        }
    }

    private static final Logger LOGGER = LogManager.getLogger(PdfTool.class);

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

    public static void main(String[] args) throws IOException {
        var pdfTool = new PdfToolCli();
        Options options = pdfTool.buildCliOptions();

        try {
            var parseConfiguration = ParseConfiguration.fromCliArguments(args, options);

            pdfTool.stampPdfFormToFile(parseConfiguration);
        } catch (ParseException e) {
            pdfTool.printUsage(options);
        }
    }

    private void stampPdfFormToFile(ParseConfiguration parseConfiguration) throws IOException {
        if (exists(parseConfiguration.filename)) {
            var result = printPreFilledPdf(parseConfiguration.filename);
            LOGGER.info("Result can be found here: {}", result.toAbsolutePath());
        } else {
            LOGGER.error("PDF file '{}' does not exist!", parseConfiguration.filename.toAbsolutePath());
        }
    }

    public Path printPreFilledPdf(Path documentPath) throws IOException {
        byte[] fileContent = readAllBytes(documentPath);
        byte[] doc = new Stamper().prefill(fileContent);
        String outputDir = System.getenv("OUTPUT_DIR");
        if (outputDir == null) {
            outputDir = System.getProperty("java.io.tmpdir");
        }

        Path out = createTempFile(Path.of(outputDir), "stamped", ".pdf");
        write(out, doc, WRITE);

        return out.toAbsolutePath();
    }

    private void printUsage(Options options) {
        new HelpFormatter().printHelp("PdfTool", options);
    }
}
