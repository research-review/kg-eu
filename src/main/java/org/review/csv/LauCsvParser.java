package org.review.csv;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.review.sources.Source;
import org.review.sources.SourceCsvSheets;
import org.review.sources.SourceType;
import org.review.sources.Sources;

/**
 * LAU CSV parser.
 * 
 * @author 33a1cc8d616a72f953d8e15274194bcd5aac2b78fbe6b4a4d1a911e0f2ef00cd
 */
public class LauCsvParser {

	private File file;
	private Source source;

	private int headingsRowIndex = -1;
	private int headingColumnIndexNutsCode = -1;
	private int headingColumnIndexLauCode = -1;
	private int headingColumnIndexLauCodeSecond = -1; // only used until 2016
	private int headingColumnIndexNameLatin = -1;
	private int headingColumnIndexNameNational = -1;
	private int headingColumnIndexPopulation = -1;
	private int headingColumnIndexArea = -1;

	public LauCsvParser(File file, Source source) {
		this.file = file;
		this.source = source;
	}

	/**
	 * Extracts country code from file name.
	 */
	public String getCountryCode() {
		return SourceCsvSheets.getLauCountryCode(file);
	}

	private void setIndividualConfig() {
		// Column "NAME_2_LAT" are empty values or "(Le)", "(L')", "(La)"
		if (source.id.equals("lau2015") && file.getName().equals("FR.csv")) {
			headingColumnIndexNameLatin = -1;
		}
	}

	/**
	 * Searches columns of headings and parses values.
	 * 
	 * @throws IOException on parsing errors
	 */
	public LauCsvCollection parse() throws IOException {

		// Get column numbers
		List<String> headings = searchHeadingsRow();
		searchHeadingColumns(headings);

		setIndividualConfig();

		// Parse rows
		LauCsvCollection lauCsvCollection = new LauCsvCollection(source.id, file);
		CSVParser csvParser = CSVParser.parse(file, StandardCharsets.UTF_8, CSVFormat.DEFAULT);

		List<String> noNumbers = Arrays.asList(new String[] { "n.a.", "n.a", "#N/A", "#VALUE!", "–" });

		String lauCode;
		String lauCodeSecond;
		String relatedNutsCode;
		String nameLatin;
		String nameNational;
		int population;
		int area;
		String tmp;
		int lauScheme;

		int rowIndex = -1;
		Iterator<CSVRecord> recordIt = csvParser.iterator();
		while (recordIt.hasNext()) {
			rowIndex++;
			CSVRecord csvRecord = recordIt.next();
			if (rowIndex <= headingsRowIndex) {
				continue;
			}

			// Reset values
			relatedNutsCode = null;
			lauCode = null;
			lauCodeSecond = null;
			nameLatin = null;
			nameNational = null;
			population = -1;
			area = -1;
			tmp = null;
			lauScheme = -1;

			// NUTS code
			relatedNutsCode = csvRecord.get(headingColumnIndexNutsCode).trim();
			if (relatedNutsCode.isEmpty())
				relatedNutsCode = null;

			// LAU code
			lauCode = csvRecord.get(headingColumnIndexLauCode).trim();
			if (lauCode.isEmpty())
				lauCode = null;

			// Skip: Comment line
			if (relatedNutsCode != null && relatedNutsCode.length() > 5)
				continue;

			// Skip: Empty rows
			if ((lauCode == null || lauCode.isEmpty()) && (relatedNutsCode == null || relatedNutsCode.isEmpty()))
				continue;

			// LAU code 2
			if (headingColumnIndexLauCodeSecond != -1)
				lauCodeSecond = csvRecord.get(headingColumnIndexLauCodeSecond).trim();
			if (lauCodeSecond != null && lauCodeSecond.isEmpty())
				lauCodeSecond = null;

			// Latin name
			if (headingColumnIndexNameLatin != -1) {
				nameLatin = csvRecord.get(headingColumnIndexNameLatin).trim();
				if (nameLatin.isEmpty())
					nameLatin = null;
			}

			// Native name
			nameNational = csvRecord.get(headingColumnIndexNameNational).trim();
			if (nameNational.isEmpty())
				nameNational = null;

			// Population
			tmp = csvRecord.get(headingColumnIndexPopulation).trim();
			if (tmp.isEmpty() || noNumbers.contains(tmp))
				population = -1;
			else
				population = Integer.parseInt(tmp.replace(" ", ""));

			// Area
			if (headingColumnIndexArea != -1) {
				tmp = csvRecord.get(headingColumnIndexArea).trim();
				if (tmp.isEmpty() || noNumbers.contains(tmp))
					area = -1;
				else
					try {
						area = Integer.parseInt(tmp);
					} catch (NumberFormatException e) {
						area = (int) Math.round(Double.parseDouble(tmp.replace(',', '.')));
					}
			}

			lauScheme = Sources.getLauScheme(SourceType.LAU, source.id);
			lauCsvCollection.add(new LauCsvItem(lauScheme, source.nutsScheme, lauCode, lauCodeSecond, relatedNutsCode,
					nameLatin, nameNational, population, area));
		}
		return lauCsvCollection;
	}

	/**
	 * Determines headings columns by text comparisons.
	 * 
	 * @param headings List of heading values to search in.
	 * 
	 * @throws RuntimeException if headings not found
	 */
	public void searchHeadingColumns(List<String> headings) {
		headingColumnIndexNutsCode = searchHeadingColumnIndex("headingColumnIndexNutsCode", headings, new String[] {
				// LAU 2017+
				"NUTS 3 CODE",
				// LAU 2015, 2016
				"NUTS_3",
				// LAU 2013
				"NUTS_3_2013_CODE",
				// LAU 2018 IT
				"NUTS 3 CODE 2013",
				// LAU 2016 UK, LAU 2015 UK
				"NUTS3_13",
				// LAU 2014 UK
				"NUTS3" });

		headingColumnIndexLauCode = searchHeadingColumnIndex("headingColumnIndexLauCode", headings, new String[] {
				// LAU 2017+
				"LAU CODE", "LAU CODE ",
				// LAU 2017 DE
				"LAU 2 CODE",
				// 1st LAU 2016
				"LAU1_NAT_CODE",
				// 1st LAU 2011
				"LAU1_CODE",
				// 1st LAU 2013 HU
				"LAU1_NAT_CODE_VER2.0" });

		headingColumnIndexLauCodeSecond = searchHeadingColumnIndex("headingColumnIndexLauCodeSecond", headings,
				new String[] {
						// 2nd LAU 2012+
						"LAU2_NAT_CODE",
						// 2nd LAU 2011
						"LAU2_CODE" });

		headingColumnIndexNameLatin = searchHeadingColumnIndex("headingColumnIndexNameLatin", headings, new String[] {
				// LAU 2017+
				"LAU NAME LATIN",
				// LAU 2016-
				"NAME_2_LAT",
				// LAU 2021 IT, LAU 2021 IT/BA/RS
				"LAU NAME alternative",
				// LAU 2013 EL
				"LAU2_NAME_2_LAT" });

		headingColumnIndexNameNational = searchHeadingColumnIndex("headingColumnIndexNameNational", headings,
				new String[] {
						// LAU 2017+
						"LAU NAME NATIONAL",
						// LAU 2016-
						"NAME_1",
						// LAU 2013 EL
						"LAU2_NAME_1" });

		headingColumnIndexPopulation = searchHeadingColumnIndex("headingColumnIndexPopulation", headings, new String[] {
				// LAU 2017+
				"POPULATION",
				// LAU 2016-
				"POP",
				// LAU 2011 CENSUS
				"GRD_POPL",
				// LAU 2016 IT
				"POP 2015" });

		headingColumnIndexArea = searchHeadingColumnIndex("headingColumnIndexArea", headings, new String[] {
				// LAU 2018+
				"TOTAL AREA (m2)",
				// LAU 2018-2020
				"TOTAL AREA (km2)",
				// LAU 2017
				"TOTAL AREA",
				// LAU 2016-
				"AREA" });

		// Check if everything set
		// headingColumnIndexLauCodeSecond not included as only used until 2016
		// headingColumnIndexArea not included as not used in 2011 CENSUS
		int arrayIndex = -1;
		for (int index : new int[] { headingColumnIndexNutsCode, headingColumnIndexLauCode, headingColumnIndexNameLatin,
				headingColumnIndexNameNational, headingColumnIndexPopulation, headingColumnIndexArea }) {
			arrayIndex++;
			if (index == -1) {
				System.err.println("Warning: Column index not set. " + source.id + " " + arrayIndex + " " + headings
						+ " " + file.getName());
			}
		}
	}

	/**
	 * Determines headings columns by text comparisons.
	 * 
	 * @return column index
	 */
	private int searchHeadingColumnIndex(String indexTitle, List<String> headings, String[] equals) {
		int indexFound = -1;
		int columnIndex = -1;
		for (String columnHeading : headings) {
			columnIndex++;
			for (String string : equals) {
				if (columnHeading.equals(string)) {
					if (indexFound == -1) {
						indexFound = columnIndex;
					} else {
						System.err.println("Warning: Column index already found  " + source.id + " " + file + "  "
								+ indexTitle + " " + indexFound + " " + string + " " + headings);
					}
				}
			}
		}
		return indexFound;
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
				if (value.equals("LAU NAME NATIONAL")) {
					this.headingsRowIndex = rowIndex;
				}

				// LAU 2016
				else if (value.equals("LAU2_NAT_CODE")) {
					this.headingsRowIndex = rowIndex;
				}

				// LAU 2011 CENSUS
				else if (value.equals("LAU1_CODE")) {
					this.headingsRowIndex = rowIndex;
				}

				values.add(value);
			}
		}
		// Found in last iteration
		if (this.headingsRowIndex != -1) {
			return values;
		}
		throw new RuntimeException("LAU headings row not found in " + this.file.getAbsolutePath() + " " + values);
	}
}