package org.review.csv;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.review.Dev;
import org.review.sources.Source;

/**
 * NUTS CSV parser
 * 
 * @author 33a1cc8d616a72f953d8e15274194bcd5aac2b78fbe6b4a4d1a911e0f2ef00cd
 */
public class NutsCsvParser {

	private File file;
	private Source source;
	private int headingsRowIndex = -1;
	private int headingColumnIndexCode = -1;
	private int headingColumnIndexCountry = -1;
	private int headingColumnIndexNuts1 = -1;
	private int headingColumnIndexNuts2 = -1;
	private int headingColumnIndexNuts3 = -1;

	public NutsCsvParser(File file, Source source) {
		this.file = file;
		this.source = source;
	}

	/**
	 * Searches columns of headings and parses values.
	 * 
	 * @throws IOException on parsing errors
	 */
	public NutsCsvCollection parse() throws IOException {

		// Get column numbers
		searchHeadings(false);

		// Parse rows
		NutsCsvCollection nutsCsvCollection = new NutsCsvCollection(source.id);
		Set<String> nutsCodesCheckSet = new HashSet<>();
		CSVParser csvParser = CSVParser.parse(file, StandardCharsets.UTF_8, CSVFormat.DEFAULT);
		int rowIndex = -1;
		Iterator<CSVRecord> recordIt = csvParser.iterator();
		String nutsCode, value;
		while (recordIt.hasNext()) {
			rowIndex++;
			CSVRecord csvRecord = recordIt.next();
			if (rowIndex <= headingsRowIndex) {
				continue;
			}

			// Parse row values
			nutsCode = null;
			value = null;
			nutsCode = csvRecord.get(headingColumnIndexCode).trim();
			if (nutsCode.isEmpty() || nutsCode.length() > 5) {
				continue;
			}
			if (nutsCode.length() == 2) {
				// whitespace replacement: special whitespace in nuts-2016-2021 BE
				value = csvRecord.get(headingColumnIndexCountry).replace(" ", " ").trim();
			} else if (nutsCode.length() == 3) {
				value = csvRecord.get(headingColumnIndexNuts1).trim();
			} else if (nutsCode.length() == 4) {
				value = csvRecord.get(headingColumnIndexNuts2).trim();
			} else if (nutsCode.length() == 5) {
				value = csvRecord.get(headingColumnIndexNuts3).trim();
			}
			if (nutsCode == null || nutsCode.isEmpty() || value == null || value.isEmpty()) {
				if (Dev.DEV && Dev.NUTS_PRINT_EMPTY) {
					System.err.println("Info - Empty: " + nutsCode + " " + rowIndex + " " + source.id + " "
							+ getClass().getSimpleName());
				}
				continue;
			}

			if (nutsCodesCheckSet.contains(nutsCode)) {
				throw new RuntimeException("Duplicate code: " + nutsCode);
			}
			nutsCodesCheckSet.add(nutsCode);

			nutsCsvCollection.add(new NutsCsvItem(source.nutsScheme, nutsCode, value));
		}
		return nutsCsvCollection;
	}

	/**
	 * Determines headings row and columns by text comparisons.
	 * 
	 * @param printOverview If true, the heading fields are printed
	 * @throws IOException      on parsing errors
	 * @throws RuntimeException if headings not found
	 */
	public void searchHeadings(boolean printOverview) throws IOException {
		// Set this.headingsRowNumber
		// Get heading values
		List<String> headings = searchHeadingsRow();

		// Set headingColumnNumber*
		searchHeadingColumns(headings);

		if (printOverview) {
			StringBuilder sb = new StringBuilder();
			sb.append(headings);
			sb.append("\n");
			sb.append(headings.get(headingColumnIndexCode));
			sb.append(" | ");
			sb.append(headings.get(headingColumnIndexCountry));
			sb.append(" | ");
			sb.append(headings.get(headingColumnIndexNuts1));
			sb.append(" | ");
			sb.append(headings.get(headingColumnIndexNuts2));
			sb.append(" | ");
			sb.append(headings.get(headingColumnIndexNuts3));
			sb.append("\n");
			System.out.println(sb.toString());
		}
	}

	/**
	 * Determines headings columns by text comparisons.
	 * 
	 * @param headings List of heading values to search in.
	 * 
	 * @throws RuntimeException if headings not found
	 */
	public void searchHeadingColumns(List<String> headings) {
		int columnIndex = -1;
		for (String columnHeading : headings) {
			columnIndex++;
			if (columnHeading.startsWith("Code ")) {
				this.headingColumnIndexCode = columnIndex;
			} else if (columnHeading.equals("Country")) {
				this.headingColumnIndexCountry = columnIndex;
			} else if (columnHeading.equals("NUTS level 1")) {
				this.headingColumnIndexNuts1 = columnIndex;
			} else if (columnHeading.equals("NUTS level 2")) {
				this.headingColumnIndexNuts2 = columnIndex;
			} else if (columnHeading.equals("NUTS level 3")) {
				this.headingColumnIndexNuts3 = columnIndex;
			}
		}

		if (headingColumnIndexCode == -1 || headingColumnIndexCountry == -1 || headingColumnIndexNuts1 == -1
				|| headingColumnIndexNuts2 == -1 || headingColumnIndexNuts3 == -1) {
			throw new RuntimeException("NUTS headings cols not found in " + headings);
		}
	}

	/**
	 * Determines headings row by text comparisons.
	 * 
	 * If found, the line number is stored in this object and the headings values
	 * are returned.
	 * 
	 * If not found, an Exception is thrown.
	 * 
	 * @return headings values
	 * @throws IOException      on parsing errors
	 * @throws RuntimeException if headings not found
	 */
	public List<String> searchHeadingsRow() throws IOException {
		CSVParser csvParser = CSVParser.parse(file, StandardCharsets.UTF_8, CSVFormat.DEFAULT);
		Iterator<CSVRecord> recordIt = csvParser.iterator();
		int rowIndex = -1;
		List<String> values = new LinkedList<>();
		while (recordIt.hasNext()) {
			// Found in previous iteration
			if (this.headingsRowIndex != -1) {
				return values;
			}

			// Search for heading value(s)
			rowIndex++;
			values.clear();
			Iterator<String> valueIt = recordIt.next().iterator();
			while (valueIt.hasNext()) {
				String value = valueIt.next();
				if (value.equals("NUTS level 3")) {
					this.headingsRowIndex = rowIndex;
				}
				values.add(value);
			}
		}
		throw new RuntimeException("NUTS headings row not found in " + this.file.getAbsolutePath());
	}
}