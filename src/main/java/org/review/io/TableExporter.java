package org.review.io;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Exports lists of Strings to tables.
 * 
 * @author 33a1cc8d616a72f953d8e15274194bcd5aac2b78fbe6b4a4d1a911e0f2ef00cd
 */
public class TableExporter {

	private List<String> headings = new LinkedList<>();
	private List<List<String>> rows = new LinkedList<>();

	private String newLine = "\n";

	public TableExporter setHeadings(List<String> headings) {
		this.headings = headings;
		return this;
	}

	public TableExporter setHeadings(String[] headings) {
		this.headings = Arrays.asList(headings);
		return this;
	}

	public TableExporter addRow(List<String> row) {
		this.rows.add(row);
		return this;
	}

	public TableExporter addRow(String[] row) {
		this.rows.add(Arrays.asList(row));
		return this;
	}

	public String getMarkdown() {
		int[] maxLengths = computeMaxLengths();
		String[] formats = new String[maxLengths.length];
		for (int i = 0; i < formats.length; i++) {
			if (maxLengths[i] == 0)
				formats[i] = "%s";
			else
				formats[i] = "%-" + maxLengths[i] + "s";
		}
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < headings.size(); i++) {
			sb.append("|");
			sb.append(String.format(formats[i], headings.get(i)));
		}
		for (int i = headings.size(); i < maxLengths.length; i++) {
			sb.append("|");
			sb.append(String.format(formats[i], ""));
		}

		sb.append("|");

		sb.append(newLine);
		for (int i = 0; i < maxLengths.length; i++) {
			sb.append("|");
			for (int j = 0; j < maxLengths[i]; j++) {
				sb.append("-");
			}
		}
		sb.append("|");

		for (List<String> row : rows) {
			sb.append(newLine);
			for (int i = 0; i < row.size(); i++) {
				sb.append("|");
				sb.append(String.format(formats[i], row.get(i)));
			}
			for (int i = row.size(); i < maxLengths.length; i++) {
				sb.append("|");
				sb.append(String.format(formats[i], ""));
			}
			sb.append("|");
		}
		return sb.toString();
	}

	private int[] computeMaxLengths() {
		int maxColumnNumbers = 0;
		if (headings.size() > maxColumnNumbers)
			maxColumnNumbers = headings.size();
		for (List<String> row : rows) {
			if (row.size() > maxColumnNumbers)
				maxColumnNumbers = row.size();
		}

		int[] maxLengths = new int[maxColumnNumbers];
		for (int i = 0; i < headings.size(); i++) {
			if (headings.get(i).length() > maxLengths[i])
				maxLengths[i] = headings.get(i).length();
		}
		for (List<String> row : rows) {
			for (int i = 0; i < row.size(); i++) {
				if (row.get(i).length() > maxLengths[i])
					maxLengths[i] = row.get(i).length();
			}
		}
		return maxLengths;
	}

}