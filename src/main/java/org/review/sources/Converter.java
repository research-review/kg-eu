package org.review.sources;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.review.Config;
import org.review.exceptions.CommandRuntimeException;

/**
 * Converts XLS to XLSX to CSV
 * 
 * 
 * LibreOffice ("LibreOffice 7.3.7.2 30(Build:2)")
 * 
 * https://libreoffice.org
 * 
 * Command: "libreoffice --convert-to xlsx file.xls --outdir directory/"
 * 
 * 
 * ssconvert / Gnumeric ("ssconvert Version »1.12.51«")
 * 
 * https://manpages.debian.org/stable/gnumeric/ssconvert.1.en.html
 * 
 * Installation: sudo apt-get install gnumeric
 * 
 * Command: ssconvert -S file.xlsx file.csv ("-S" means export one file per
 * sheet)
 * 
 * 
 * in2csv ("in2csv 1.0.7")
 * 
 * https://csvkit.readthedocs.io/en/latest/scripts/in2csv.html
 * 
 * Installation: "pip install csvkit"
 * 
 * Command: "in2csv --write-sheets - file.xlsx"
 * 
 * 
 * xlsx2csv ("0.8")
 * 
 * https://github.com/dilshod/xlsx2csv
 * 
 * Installation: "pip install xlsx2csv"
 * 
 * Command: "xlsx2csv -s 0 file.xlsx directory/" ("-s 0" means all sheets)
 * 
 * Note: xlsx2csv execution worked partly when Eclipse was started from command line.
 * 
 * 
 * @author 33a1cc8d616a72f953d8e15274194bcd5aac2b78fbe6b4a4d1a911e0f2ef00cd
 */
public class Converter {

	/**
	 * Executes a shell command.
	 */
	public String executeCommand(String... command) {
		StringBuilder stringBuilder = new StringBuilder();
		ProcessBuilder processBuilder = new ProcessBuilder();
		List<String> cmd = new ArrayList<>(command.length + 2);
		cmd.add("sh");
		cmd.add("-c");
		cmd.addAll(Arrays.asList(command));
		processBuilder.command(cmd);
		try {
			Process process = processBuilder.start();
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;
			while ((line = reader.readLine()) != null) {
				stringBuilder.append(line + "\n");
			}
			int exitValue = process.waitFor();
			if (exitValue != 0) {
				stringBuilder.append("Error: Process exited with " + exitValue + "\n");
				throw new CommandRuntimeException(stringBuilder.toString(), exitValue);
			}
		} catch (IOException e) {
			throw new CommandRuntimeException(e);
		} catch (InterruptedException e) {
			throw new CommandRuntimeException(e);
		}
		return stringBuilder.toString();
	}

	/**
	 * @return false, if the LibreOffice command does not return 0
	 */
	public boolean isLibreofficeInstalled() {
		try {
			executeCommand("libreoffice --version");
		} catch (CommandRuntimeException e) {
			if (e.exitValue != null)
				return false;
			else
				throw e;
		}
		return true;
	}

	/**
	 * @return false, if the xlsx2csv command does not return 0
	 */
	public boolean isXlsx2csvInstalled() {
		try {
			executeCommand("xlsx2csv --version");
		} catch (CommandRuntimeException e) {
			if (e.exitValue != null) {
				System.out.println(e.exitValue);
				return false;
			} else
				throw e;
		}
		return true;
	}

	/**
	 * @return false, if the in2csv command does not return 0
	 */
	public boolean isIn2csvInstalled() {
		try {
			executeCommand("in2csv --version");
		} catch (CommandRuntimeException e) {
			if (e.exitValue != null) {
				System.out.println(e.exitValue);
				return false;
			} else
				throw e;
		}
		return true;
	}

	/**
	 * @return false, if the in2csv command does not return 0
	 */
	public boolean isSsconvertInstalled() {
		try {
			executeCommand("ssconvert --version");
		} catch (CommandRuntimeException e) {
			if (e.exitValue != null) {
				System.out.println(e.exitValue);
				return false;
			} else
				throw e;
		}
		return true;
	}

	/**
	 * Converts XLS files to XLSX files.
	 */
	public void convertXls(Source source) throws IOException {
		if (source.fileType.equals(Sources.FILETYPE_XLS)) {
			String file = source.getDownloadFile().getAbsolutePath();
			if (new File(Config.get(Config.KEY_CONVERTED_DIRECTORY), source.getDownloadFileName() + "x").exists()) {
				System.out.println("Skipping as XSLX exists: " + source.getXlsxFile().getAbsolutePath());
				return;
			}
			File dir = new File(Config.get(Config.KEY_CONVERTED_DIRECTORY));
			if (!dir.exists())
				dir.mkdirs();
			String cmd = "libreoffice --convert-to xlsx " + file + " --outdir " + dir.getAbsolutePath();
			System.out.println(executeCommand(cmd));
		}
	}

	/**
	 * Converts XLSX files to CSV files using xlsx2csv.
	 */
	public void convertCsvXlsx2csv() throws IOException {
		for (Source source : new Sources().getSources()) {
			if (!source.fileType.equals(Sources.FILETYPE_XLSX) && !source.fileType.equals(Sources.FILETYPE_XLS))
				continue;
			String file = source.getXlsxFile().getAbsolutePath();
			File dir = source.getCsvDirectory();
			if (dir.exists())
				continue;
			String cmd = "xlsx2csv -s 0 " + file + " " + dir.getAbsolutePath();
			System.out.println(cmd);
			System.out.println(executeCommand(cmd));
		}
	}

	/**
	 * Converts XLSX files to CSV files using in2csv.
	 * 
	 * @param source            The source to convert
	 * @param skipSheetCreation If the creation of single CSV sheets was already
	 *                          done, e.g. directly in terminal. This will only
	 *                          move/rename the created files.
	 * @throws IOException If files can not be moved
	 */
	public void convertCsvIn2csv(Source source, boolean skipSheetCreation) throws IOException {
		if (!source.fileType.equals(Sources.FILETYPE_XLSX) && !source.fileType.equals(Sources.FILETYPE_XLS)) {
			System.out.println("Skipping as not XLSX or XLS: " + source.id);
			return;
		}

		File targetDirectory = source.getCsvDirectory();
		if (targetDirectory.exists()) {
			System.out.println("Skipping as CSV exists: " + source.getCsvDirectory());
			return;
		}

		// Get sheet names
		File xslxFile = source.getXlsxFile();
		String filepath = xslxFile.getAbsolutePath();
		String cmd = "in2csv --write-sheets - -n " + filepath;
		List<String> sheetnames = Arrays.asList(executeCommand(cmd).split("\n"));

		// Convert to CSV files
		if (!skipSheetCreation) {
			cmd = "in2csv --write-sheets - " + xslxFile;
			System.out.println("Converting: " + xslxFile);
			executeCommand(cmd);
		}

		// Move generated files
		source.getCsvDirectory().mkdirs();
		for (int i = 0; i < sheetnames.size(); i++) {
			File sourceFile = new File(source.getXlsxFile().getParent(), source.id + "_" + i + ".csv");
			File targetFile = new File(targetDirectory, sheetnames.get(i) + ".csv");
			System.out.println("Moving: " + sourceFile + " -> " + targetFile);
			Files.move(sourceFile.toPath(), targetFile.toPath());
		}
	}

	/**
	 * Converts XLSX files to CSV files using ssconvert (and in2csv).
	 * 
	 * @param source The source to convert
	 * @throws IOException If files can not be moved
	 */
	public void convertSsconvert(Source source) throws IOException {
		if (!source.fileType.equals(Sources.FILETYPE_XLSX) && !source.fileType.equals(Sources.FILETYPE_XLS)) {
			System.out.println("Skipping as not XLSX or XLS: " + source.id);
			return;
		}

		File targetDirectory = source.getCsvDirectory();
		if (targetDirectory.exists()) {
			System.out.println("Skipping as CSV exists: " + source.getCsvDirectory());
			return;
		}

		// Get sheet names
		File xslxFile = source.getXlsxFile();
		File csvFile = new File(xslxFile.getParentFile(), source.id + ".csv");
		String filepath = xslxFile.getAbsolutePath();
		String cmd = "in2csv --write-sheets - -n " + filepath;
		List<String> sheetnames = Arrays.asList(executeCommand(cmd).split("\n"));

		// Convert to CSV files
		cmd = "ssconvert -S " + xslxFile.getAbsolutePath() + " " + csvFile.getAbsolutePath();
		System.out.println("Converting: " + xslxFile);
		executeCommand(cmd);

		// Move generated files
		source.getCsvDirectory().mkdirs();
		for (int i = 0; i < sheetnames.size(); i++) {
			File sourceFile = new File(source.getXlsxFile().getParent(), source.id + ".csv." + i);
			File targetFile = new File(targetDirectory, sheetnames.get(i) + ".csv");
			System.out.println("Moving: " + sourceFile + " -> " + targetFile);
			Files.move(sourceFile.toPath(), targetFile.toPath());
		}
	}
}