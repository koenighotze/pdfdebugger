package org.koenighotze.pdftool;

import static java.lang.String.format;
import static java.nio.file.Files.exists;
import static org.apache.commons.cli.Option.builder;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.lowagie.text.DocumentException;
import javaslang.Tuple;
import javaslang.Tuple3;
import javaslang.control.Try;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

/**
 * Simple tool for pre-stamping a PDF form with the keys of the fields as their
 * value.
 *
 * @author dschmitz
 */
public class PdfTool {

    public static void main(String[] args) throws IOException, DocumentException {
        Options options = new Options()
            .addOption(builder().longOpt("verbose").desc("verbose output").build())
            .addOption(builder().longOpt("numbers").desc("print numbers instead of names").build())
            .addOption(builder().longOpt("file").required().argName("PdfDoc").desc("the pdf document").hasArg().build());

        Try.of(() -> new DefaultParser().parse(options, args))
           .onFailure(ex -> printUsage(options))
           .map(PdfTool::extractOptions)
           .filter(PdfTool::ifFileExists)
           .flatMap(PdfTool::stampPdfTemplate)
           .onFailure(t -> System.err.println("File does not exist!"))
           .onSuccess(path -> System.out.println(format("Result is here: %s", path.toAbsolutePath())));
    }

    private static Try<Path> stampPdfTemplate(Tuple3<Path, Boolean, Boolean> t) {
        return Try.of(() -> new Stamper(t._1).printPreFilledPdf(t._2, t._3));
    }

    private static boolean ifFileExists(Tuple3<Path, Boolean, Boolean> t) {
        return exists(t._1);
    }

    private static void printUsage(Options options) {
        new HelpFormatter().printHelp("PdfTool", options);
    }

    private static Tuple3<Path, Boolean, Boolean> extractOptions(CommandLine cmd) {
        return Tuple.of(cmd.getOptionValue("file"), cmd.hasOption("numbers"), cmd.hasOption("verbose"))
                    .map((fname, numbers, verbose) -> Tuple.of(Paths.get(fname), numbers, verbose));
    }

}
