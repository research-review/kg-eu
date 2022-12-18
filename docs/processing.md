# review data processing

## Resource files

- Source files are listed in [sources.json](../sources.json). A metadata entry consists of:
	- *id*: A unique ID.
	- *type*: e.g. nuts or lau.
	- *filetype*: e.g. xls or xlsx.
	- *sources*: At least one download URL.
- To add additional files to the processing queue, add a respective entry.

## Download

- The download URLs listed in [sources.json](../sources.json) are used for downloading data.
- The used files are mirrored at the [Hobbit FTP server](https://hobbitdata.informatik.uni-leipzig.de/review/sources/download/).

## Data conversion

- Excel files are converted into CSV files. Currently, the following approach is used:
	- Use [LibreOffice](https://libreoffice.org) to convert XLS into XLSX.
	- Use [ssconvert](https://manpages.debian.org/stable/gnumeric/ssconvert.1.en.html) (Gnumeric) to convert XLSX into CSV.
	- Use [in2csv](https://csvkit.readthedocs.io/en/latest/scripts/in2csv.html) (csvkit) to get XLSX sheet names.
- The converted files are mirrored at the [Hobbit FTP server](https://hobbitdata.informatik.uni-leipzig.de/review/sources/csv/).

### Previous data conversion approaches

#### Apache POI

-

#### LibreOffice

Libreoffice 7.3 could not open a XLSX file (LAU 2020), as the maximum number of columns was too large.

#### xlsx2csv

[xlsx2csv](https://github.com/dilshod/xlsx2csv) was not able to process at least one large file.

#### Google Spreadsheets

Google Spreadsheets stated that a file was too large to open.

