package org.review.sources;

import java.io.File;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;

import org.review.io.Io;

/**
 * Methods to access single sheets available as CSV data.
 * 
 * @author 33a1cc8d616a72f953d8e15274194bcd5aac2b78fbe6b4a4d1a911e0f2ef00cd
 */
public class SourceCsvSheets {

	public static final int MINIMAL_CSV_FILE_SIZE = 450;

	/**
	 * Extracts country code from file name.
	 */
	public static String getLauCountryCode(File file) {
		return file.getName().substring(0, file.getName().lastIndexOf("."));
	}

	private Source source;

	public SourceCsvSheets(Source source) {
		this.source = source;
	}

	/**
	 * Searches in CSV directory for main NUTS file and returns it.
	 * 
	 * For 1995 to 2001 data, main NUTS file names start with "NUTS".
	 */
	public File getNutsMainSheetFile() {
		if (source.sourceType.equals(SourceType.NUTS)) {
			// Collect all CSV files starting with NUTS
			List<File> nutsFiles = new LinkedList<>();
			for (URI fileUri : Io.listDirectory(source.getCsvDirectory().getAbsolutePath())) {
				File file = new File(fileUri);
				if (file.getName().startsWith("NUTS")) {
					nutsFiles.add(file);
				}
			}
			// Return unique file or throw Exception
			if (nutsFiles.size() == 1) {
				return nutsFiles.get(0);
			} else {
				throw new RuntimeException("Did not find an unique NUTS CSV file: " + nutsFiles);
			}
		} else
			throw new RuntimeException("Not a NUTS source: " + source.id);
	}

	/**
	 * Searches in CSV directory for LAU files and returns them.
	 * 
	 * A typical filename is "DE.csv". Some files without main content consist of
	 * 434 Byte (e.g. in lau2020-nuts2016) or less.
	 */
	public List<File> getLauSheetFiles() {
		if (source.sourceType.equals(SourceType.LAU)) {
			List<File> lauFiles = new LinkedList<>();
			for (URI fileUri : Io.listDirectory(source.getCsvDirectory().getAbsolutePath())) {
				File file = new File(fileUri);
				if (file.getName().length() == 6 && file.length() > MINIMAL_CSV_FILE_SIZE) {
					lauFiles.add(file);
				}
			}
			return lauFiles;
		} else
			throw new RuntimeException("Not a LAU source: " + source.id);
	}

}