# PDF Debugger

Simple tool for pre-stamping a pdf form.

Current build status: [![CircleCI](https://circleci.com/gh/koenighotze/pdfdebugger.svg?style=svg)](https://circleci.com/gh/koenighotze/pdfdebugger)  [![Codacy Badge](https://api.codacy.com/project/badge/Grade/2082d38336fa495c8a91851ebb297793)](https://www.codacy.com/app/david-schmitz-privat/pdfdebugger?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=koenighotze/pdfdebugger&amp;utm_campaign=Badge_Grade)

## Usage

There is a Docker version available on [Docker Hub](https://cloud.docker.com/u/koenighotze/repository/docker/koenighotze/pdfdebugger).

You can use it like this:

```bash
$ docker run --rm=true -v ${PWD}:/app/in -v ${PWD}/out:/app/out koenighotze/pdfdebugger:2.0 --file /app/in/interactiveform_enabled.pdf
PDF is in Version 6 and has 1 pages
Stamping key ZIP (text)
...
Stamping key Emergency_Contact (text)
Result is here: /app/out/stamped2668326534888947129pdf
```

There are two volumes `/app/in`, which should contain the input-PDF and `/app/out` which will contain the stamped PDF file.

See below for building and running a local version.

## Building

Build it like this: 

```bash
$ mvn package
```

Usage:
```
PdfTool
    --file <PdfDoc>   the pdf document
    --numbers         print numbers instead of names
    --verbose         verbose output
```

and use it like this:

```bash

$ java -jar pdftool-<VERSION>.jar --file YOUR_PDF_FORM.pdf --verbose
...
Result is here: /var/folders/fj/m9dg31412tgg3v1l3ly23145t6j5nw/T/stamped8992316045665224650.pdf
```

The result file contains the stamped version.
