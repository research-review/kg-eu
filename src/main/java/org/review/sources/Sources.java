package org.review.sources;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.review.Config;

/**
 * Parses JSON file with source metadata and creates {@link Source} objects.
 * 
 * @author 33a1cc8d616a72f953d8e15274194bcd5aac2b78fbe6b4a4d1a911e0f2ef00cd
 */
public class Sources {

	public static final String KEY_ID = "id";
	public static final String KEY_FILETYPE = "filetype";
	public static final String KEY_SOURCES = "sources";
	public static final String KEY_TYPE = "type";
	public static final String KEY_NUTS_SCHEME = "nuts-scheme";

	public static final String FILETYPE_XLSX = "xlsx";
	public static final String FILETYPE_XLS = "xls";

	/**
	 * Extracts LAU scheme from ID.
	 */
	public static int getLauScheme(SourceType sourceType, String sourceId) {
		if (sourceType.equals(SourceType.LAU)) {
			// e.g. lau2021-nuts2021 or lau2016
			return Integer.parseInt(sourceId.substring(3, 7));
		} else {
			throw new RuntimeException("Not a " + SourceType.LAU + " type");
		}
	}

	/**
	 * Parses JSON file and returns list of {@link Source} IDs.
	 */
	public List<String> getSourceIds() {
		List<String> ids = new LinkedList<>();
		try {
			for (Source source : getSources()) {
				ids.add(source.id);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return ids;
	}

	/**
	 * Parses JSON file and returns list of {@link Source} objects.
	 */
	public List<Source> getSources() throws IOException {
		return this.parseJsonFile(new File(Config.get(Config.KEY_SOURCES_FILE)));
	}

	/**
	 * Parses JSON file and returns list of {@link Source} objects.
	 * 
	 * @param file JSON file
	 * @return List of Source objects
	 * @throws IOException
	 */
	private List<Source> parseJsonFile(File file) throws IOException {
		List<Source> sources = new LinkedList<>();
		String json = Files.readString(file.toPath());
		JSONArray jsonArray = new JSONArray(json);
		for (int i = 0; i < jsonArray.length(); i++) {
			sources.add(new Source(jsonArray.getJSONObject(i)));
		}
		return sources;
	}
}