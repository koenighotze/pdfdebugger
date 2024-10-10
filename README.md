# PDF Debugger

Simple tool for pre-stamping a pdf form.

[![CI](https://github.com/koenighotze/pdfdebugger/actions/workflows/ci.yml/badge.svg)](https://github.com/koenighotze/pdfdebugger/actions/workflows/ci.yml)
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/2082d38336fa495c8a91851ebb297793)](https://www.codacy.com/gh/koenighotze/pdfdebugger/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=koenighotze/pdfdebugger&amp;utm_campaign=Badge_Grade)
[![Codacy Badge](https://app.codacy.com/project/badge/Coverage/2082d38336fa495c8a91851ebb297793)](https://app.codacy.com/gh/koenighotze/pdfdebugger/dashboard?utm_source=gh&utm_medium=referral&utm_content=&utm_campaign=Badge_coverage)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=koenighotze_pdfdebugger&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=koenighotze_pdfdebugger)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=koenighotze_pdfdebugger&metric=vulnerabilities)](https://sonarcloud.io/summary/new_code?id=koenighotze_pdfdebugger)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=koenighotze_pdfdebugger&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=koenighotze_pdfdebugger)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=koenighotze_pdfdebugger&metric=bugs)](https://sonarcloud.io/summary/new_code?id=koenighotze_pdfdebugger)

## Usage

There is a Docker version available on [Docker Hub](https://cloud.docker.com/u/koenighotze/repository/docker/koenighotze/pdfdebugger).

You can use it like this:

```bash
$ docker run \
        --rm=true \
        -v ${PWD}:/app/in \ 
        -v ${PWD}/out:/app/out \
        koenighotze/pdfdebugger:3.4 \
        --file /app/in/src/test/resources/interactiveform_enabled.pdf \
        --target-dir /app/out

...
2024-10-10 08:30:58,343 [main]  INFO o.k.p.PdfToolCli - Result can be found here: /app/out/stamped671063627034583030.pdf
```

Here is what is going on:

- `-v ${PWD}:/app/in`: Input volume containing the input PDF
- `-v ${PWD}/out:/app/out`: The output volume where the stamped PDF will be created
- `koenighotze/pdfdebugger:3.4`: The image and version used, replace `3.4` with whatever you want to use
- `--file /app/in/src/test/resources/interactiveform_enabled.pdf`: This tells the tool where the source PDF form document can be found; note that the configured input volume `/app/in` is used
- `--target-dir /app/out`: This tells the tool where the stamped PDF document should be created; note that the configured output volume `/app/out` is used
- `Result can be found here: /app/out/stamped671063627034583030.pdf`: The final log message tells you the full path of the stamped PDF document; note that the configured output volume `/app/out` is used. In the example above, the stamped document is in `${PWD}/out` on the local machine.

See below for building and running a local version.

## Building

Build it like this:

```bash
$ ./mvnw package
...
❯ java -jar target/pdftool-<VERSION>.jar
usage: PdfToolCli
    --file <PdfDoc>                   the pdf document
    --target-dir <target directory>   location where the stamped PDF will
                                      be written to; defaults to the
                                      systems temp directory
```

and use it like this:

```bash

$ java -jar pdftool-<VERSION>.jar --file YOUR_PDF_FORM.pdf 
...
Result is here: /var/folders/fj/m9dg31412tgg3v1l3ly23145t6j5nw/T/stamped8992316045665224650.pdf
```

The result file contains the stamped version.

## TODO

- [ ] Move to Java 21
- [x] Container scan
- [x] Container upload
- [ ] Profiling
- [ ] Integration test
- [ ] Upload Javadoc
- [ ] Release
- [x] Setup commit sign force
- [x] More linting
- [x] Drop iText
- [x] Drop vavr
