package org.koenighotze.pdftool;

import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.apache.commons.cli.Option.builder;
import static org.koenighotze.pdftool.PdfToolCli.ParseConfiguration.fromCliArguments;

public class PdfToolCli {
    private static final Logger LOGGER = LogManager.getLogger(PdfToolCli.class);

    record ParseConfiguration(Path filename, Path targetDirectory) {
        static ParseConfiguration fromCliArguments(String[] args, Options options) throws ParseException {
            var parsedOptions = new DefaultParser().parse(options, args);
            return new ParseConfiguration(
                    Paths.get(parsedOptions.getOptionValue("file")),
                    Paths.get(parsedOptions.getOptionValue("target-dir", System.getProperty("java.io.tmpdir"))));
        }
    }

    public static void main(String[] args) throws IOException {
        var pdfTool = new PdfToolCli();
        Options options = pdfTool.buildCliOptions();

        try {
            var parseConfiguration = fromCliArguments(args, options);
            var result = new PdfTool().printPreFilledPdf(parseConfiguration.filename, parseConfiguration.targetDirectory);
            LOGGER.info("Result can be found here: {}", result.toAbsolutePath());
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

    private void printUsage(Options options) {
        new HelpFormatter().printHelp("PdfToolCli", options);
    }
}
