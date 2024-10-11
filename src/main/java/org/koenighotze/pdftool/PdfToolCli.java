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
import static java.nio.file.StandardOpenOption.WRITE;
import static org.apache.commons.cli.Option.builder;
import static org.koenighotze.pdftool.PdfToolCli.ParseConfiguration.fromCliArguments;

public class PdfToolCli {
    record ParseConfiguration(Path filename, Path targetDirectory) {
        static ParseConfiguration fromCliArguments(String[] args, Options options) throws ParseException {
            var parsedOptions = new DefaultParser().parse(options, args);
            return new ParseConfiguration(
                    Paths.get(parsedOptions.getOptionValue("file")),
                    Paths.get(parsedOptions.getOptionValue("target-dir", System.getProperty("java.io.tmpdir"))));
        }
    }

    private static final Logger LOGGER = LogManager.getLogger(PdfToolCli.class);

    public static void main(String[] args) throws IOException {
        var pdfTool = new PdfToolCli();
        Options options = pdfTool.buildCliOptions();

        try {
            var parseConfiguration = fromCliArguments(args, options);

            pdfTool.stampPdfFormToFile(parseConfiguration);
        } catch (ParseException e) {
            pdfTool.printUsage(options);
        }
    }

    private Options buildCliOptions() {
        return new Options()
                .addOption(builder().longOpt("target-dir")
                        .argName("target directory")
                        .desc("location where the stamped PDF will be written to; defaults to the systems temp directory")
                        .hasArg()
                        .build())
                .addOption(builder().longOpt("file")
                        .required()
                        .argName("PdfDoc")
                        .desc("the pdf document")
                        .hasArg()
                        .build());
    }

    private void stampPdfFormToFile(ParseConfiguration parseConfiguration) throws IOException {
        if (!exists(parseConfiguration.filename)) {
            LOGGER.error("PDF file '{}' does not exist!", parseConfiguration.filename.toAbsolutePath());
            return;
        }
        var result = printPreFilledPdf(parseConfiguration.filename, parseConfiguration.targetDirectory);
        LOGGER.info("Result can be found here: {}", result.toAbsolutePath());
    }

    public Path printPreFilledPdf(Path documentPath, Path targetPath) throws IOException {
        var doc = new Stamper().prefill(readAllBytes(documentPath));

        var out = createTempFile(targetPath, "stamped", ".pdf");
        write(out, doc, WRITE);

        return out.toAbsolutePath();
    }

    private void printUsage(Options options) {
        new HelpFormatter().printHelp("PdfToolCli", options);
    }
}
