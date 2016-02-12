# pdfdebugger

Simple tool for pre-stamping a pdf form.

Current build status: [![Build Status](https://travis-ci.org/koenighotze/pdfdebugger.svg?branch=master)](https://travis-ci.org/koenighotze/pdfdebugger)

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



