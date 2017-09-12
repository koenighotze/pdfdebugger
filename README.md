# pdfdebugger

Simple tool for pre-stamping a pdf form.

Current build status: [![Build Status](https://travis-ci.org/koenighotze/pdfdebugger.svg?branch=master)](https://travis-ci.org/koenighotze/pdfdebugger)  

[![Codacy Badge](https://api.codacy.com/project/badge/Grade/2082d38336fa495c8a91851ebb297793)](https://www.codacy.com/app/david-schmitz-privat/pdfdebugger?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=koenighotze/pdfdebugger&amp;utm_campaign=Badge_Grade)

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



Todo: https://danielflower.github.io/2015/01/29/Generating-a-Maven-plugin-site-and-publishing-to-Github-Pages.html