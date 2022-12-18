package org.review;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.review.csv.LauCsvParser;
import org.review.csv.NutsCsvParser;
import org.review.rdf.ModelBuilder;
import org.review.sources.Converter;
import org.review.sources.Source;
import org.review.sources.SourceCsvSheets;
import org.review.sources.SourceType;
import org.review.sources.Sources;

/**
 * review - Command Line Interface.
 *
 * @author 33a1cc8d616a72f953d8e15274194bcd5aac2b78fbe6b4a4d1a911e0f2ef00cd
 */
public class Main {

	public static final String MODE_LIST = "ls";
	public static final String MODE_DOWNLOAD = "dl";
	public static final String MODE_CSV = "csv";
	public static final String MODE_COUNTRIES = "ct";
	public static final String MODE_GRAPH = "kg";
	public static final String MODE_HELP = "help";

	public static final String OPTION_IDS = "ids";
	public static final String OPTION_COUNTRIES = "countries";

	public static StringBuilder helpTextBuilder;
	public static Map<String, String> modes;

	public static void main(String[] args) throws IOException {

		// File check
		File configurationFile = new File(Config.CONFIGURATION_FILE);
		if (!configurationFile.canRead()) {
			System.err.println("Error: Can not read file " + configurationFile.getAbsolutePath());
			return;
		}
		File sourcesFile = new File(Config.get(Config.KEY_SOURCES_FILE));
		if (!sourcesFile.canRead()) {
			System.err.println("Error: Can not read file " + sourcesFile.getAbsolutePath());
			return;
		}

		// Defaults
		String mode = MODE_HELP;
		List<String> sourceIds = new Sources().getSourceIds();
		List<String> countyCodes = new LinkedList<>();

		// List of all available modes
		modes = new LinkedHashMap<>();
		modes.put(MODE_LIST, "  Lists available dataset IDs");
		modes.put(MODE_DOWNLOAD, "  Downloads Excel files");
		modes.put(MODE_CSV, " Converts Excel files to CSV");
		modes.put(MODE_COUNTRIES, "  Lists available LAU countries");
		modes.put(MODE_GRAPH, "  Create knowledge graph");
		modes.put(MODE_HELP, "Prints this help");
		if (Dev.DEV)
			modes.put(Dev.MODE, " Development mode");

		// Build help text
		helpTextBuilder = new StringBuilder().append("\n" + "Modes:" + "\n");
		for (Entry<String, String> entry : modes.entrySet()) {
			helpTextBuilder.append("  " + entry.getKey() + ": " + entry.getValue() + "\n");
		}
		helpTextBuilder.append("Options:\n");

		// Parser with options
		Options options = new Options()
				.addOption(Option.builder(OPTION_IDS).hasArg(true).argName("\"ID1 ID2 ...\"").build())
				.addOption(Option.builder(OPTION_COUNTRIES).hasArg(true).argName("\"C1 C2 ...\"").build());
		DefaultParser parser = new DefaultParser();

		// Parse arguments and options
		try {

			// Use developments arguments
			if (Dev.DEV)
				args = Dev.DEV_ARGS;

			CommandLine commandLine = parser.parse(options, args);

			// Set mode
			List<String> arguments = Arrays.asList(commandLine.getArgs());
			if (arguments.size() > 1)
				throw new ParseException("Error: Multiple modes given:" + arguments);
			else if (arguments.size() == 1) {
				mode = arguments.get(0);
				if (!modes.containsKey(mode))
					throw new ParseException("Error: Unknown mode: " + mode);
			}

			// Set IDs
			if (commandLine.hasOption(OPTION_IDS)) {
				List<String> newIds = Arrays.asList(commandLine.getOptionValue(OPTION_IDS).split(" "));
				for (String newId : newIds) {
					if (!sourceIds.contains(newId)) {
						throw new ParseException("Error: Unknown ID: " + newId);
					}
				}
				sourceIds = newIds;
			}

			// Set countries
			if (commandLine.hasOption(OPTION_COUNTRIES)) {
				for (String countryCode : commandLine.getOptionValue(OPTION_COUNTRIES).split(" ")) {
					countyCodes.add(countryCode);
				}
			}
		} catch (ParseException e) {
			System.err.println(e.getMessage());
			mode = MODE_HELP;
		}

		// Print configuration
		if (Dev.DEV) {
			StringBuilder sb = new StringBuilder();
			sb.append("DEV").append("\n");
			sb.append("Mode: " + mode).append("\n");
			sb.append("IDs:  " + sourceIds).append("\n");
			System.err.print(sb.toString());
		}

		// Run: Print IDs
		if (mode.equals(MODE_LIST)) {
			System.out.println("Available dataset IDs:");
			for (String sourceId : new Sources().getSourceIds()) {
				System.out.println(sourceId);
			}
		}

		// Run: Download datasets
		else if (mode.equals(MODE_DOWNLOAD)) {
			for (Source source : new Sources().getSources()) {
				if (sourceIds.contains(source.id))
					source.download();
			}
		}

		// Run: Convert to CSV
		else if (mode.equals(MODE_CSV)) {
			Converter converter = new Converter();
			boolean libreoffice = converter.isLibreofficeInstalled();
			boolean xlsx2csv = converter.isIn2csvInstalled();
			boolean ssconvert = converter.isSsconvertInstalled();
			for (Source source : new Sources().getSources()) {
				if (sourceIds.contains(source.id)) {
					// Convert XLS -> XLSX
					if (source.fileType.equals(Sources.FILETYPE_XLS)) {
						if (!libreoffice) {
							System.out.println("Skipping XLS convertion as LibreOffice not found: " + source.id);
						} else {
							converter.convertXls(source);
						}
					}
					// Convert XLSX -> CSV
					if (!xlsx2csv) {
						System.out.println("Skipping CSV convertion as csvIn2csv not found: " + source.id);
					} else if (!ssconvert) {
						System.out.println("Skipping CSV convertion as ssconvert not found: " + source.id);
					} else {
						converter.convertSsconvert(source);
					}
				}
			}
		}

		// Run: List LAU countries
		else if (mode.equals(MODE_COUNTRIES)) {
			SortedSet<String> countryCodes = new TreeSet<>();
			for (Source source : new Sources().getSources()) {
				if (source.sourceType.equals(SourceType.LAU)) {
					if (sourceIds.contains(source.id)) {
						SourceCsvSheets sourceCsvSheets = new SourceCsvSheets(source);
						for (File file : sourceCsvSheets.getLauSheetFiles()) {
							countryCodes.add(SourceCsvSheets.getLauCountryCode(file));
						}
					}
				}
			}
			System.out.println(countryCodes);
		}

		// Run: Create graph
		else if (mode.equals(MODE_GRAPH)) {
			ModelBuilder modelBuilder = new ModelBuilder();
			for (Source source : new Sources().getSources()) {
				if (sourceIds.contains(source.id)) {
					if (source.sourceType.equals(SourceType.LAU)) {
						SourceCsvSheets sourceCsvSheets = new SourceCsvSheets(source);
						for (File file : sourceCsvSheets.getLauSheetFiles()) {
							if (countyCodes.isEmpty() || countyCodes.contains(SourceCsvSheets.getLauCountryCode(file)))
								modelBuilder.lauCsvCollections.add(new LauCsvParser(file, source).parse());
						}
					}
					if (source.sourceType.equals(SourceType.NUTS)) {
						SourceCsvSheets sourceCsvSheets = new SourceCsvSheets(source);
						modelBuilder.nutsCsvCollections
								.add(new NutsCsvParser(sourceCsvSheets.getNutsMainSheetFile(), source).parse());
					}
				}
			}
			Model model = modelBuilder.build();
			System.out.println("Created graph with " + model.size() + " triples");
			File file = new File(Config.get(Config.KEY_GENERATED_DIRECTORY), "model.nt");
			file.getParentFile().mkdirs();
			FileOutputStream fos = new FileOutputStream(file);
			RDFDataMgr.write(fos, model, Lang.NTRIPLES);
			fos.close();
			System.out.println("Wrote " + file);
		}

		// Run: Print help
		else if (mode.equals(MODE_HELP)) {
			HelpFormatter helpFormatter = new HelpFormatter();
			StringBuilder sb = new StringBuilder();
			sb.append("Usage:   COMMAND [OPTIONS] MODE\n");
			sb.append("Example: java -jar launuts.jar -ids \"nuts-2016-2021 lau2021-nuts2021\" dl");
			helpFormatter.setSyntaxPrefix(sb.toString());
			helpFormatter.printHelp(helpTextBuilder.toString(), options);
		}

		// Run: Development
		else if (mode.equals(Dev.MODE)) {
			Dev.dev(sourceIds, countyCodes);
		}
	}
}