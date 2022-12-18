package org.review;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

/**
 * Configuration values parsed from JSON file.
 * 
 * Usage: Config.get(Config.KEY_***).
 * 
 * Implemented as Singleton, see https://www.baeldung.com/java-singleton
 *
 * @author 33a1cc8d616a72f953d8e15274194bcd5aac2b78fbe6b4a4d1a911e0f2ef00cd
 */
public enum Config {

	INSTANCE();

	public static String get(String key) {
		return Config.INSTANCE.getInstance().configuration_values.get(key);
	}

	public static final String CONFIGURATION_FILE = "configuration.json";

	public static final String KEY_SOURCES_FILE = "sources-file";
	public static final String KEY_DOWNLOAD_DIRECTORY = "download-directory";
	public static final String KEY_CONVERTED_DIRECTORY = "converted-directory";
	public static final String KEY_CSV_DIRECTORY = "csv-directory";
	public static final String KEY_GENERATED_DIRECTORY = "generated-directory";

	private Map<String, String> configuration_values;

	public Config getInstance() {
		return getInstance(CONFIGURATION_FILE);
	}

	/**
	 * Reads configuration from JSON file and returns instance.
	 */
	public Config getInstance(String file) {
		if (configuration_values == null) {
			configuration_values = new HashMap<>();
			String json = "{}";
			try {
				json = Files.readString(new File(file).toPath());
			} catch (IOException e) {
				System.err.println("Error while reading configuration file " + new File(file).getAbsolutePath());
				System.exit(1);
			}
			JSONObject jsonObject = new JSONObject(json);
			for (String key : jsonObject.keySet()) {
				configuration_values.put(key, jsonObject.getString(key));
			}
		}
		return INSTANCE;
	}

}