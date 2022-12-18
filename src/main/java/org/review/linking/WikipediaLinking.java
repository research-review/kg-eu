package org.review.linking;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;
import org.review.Config;
import org.review.io.Io;

/**
 * Creates Wikipedia links for NUTS level 0 and 1.
 * 
 * Uses
 * https://en.wikipedia.org/w/index.php?title=First-level_NUTS_of_the_European_Union&oldid=1126125069
 * 
 * @author 33a1cc8d616a72f953d8e15274194bcd5aac2b78fbe6b4a4d1a911e0f2ef00cd
 */
public class WikipediaLinking {

	public static final String PREFIX_WP_OLDID = "https://en.wikipedia.org/w/api.php?action=parse&prop=wikitext&format=json&oldid=";
	public static final String WPNUTS1_OLDID = "1126125069";
	public static final String WPNUTS1_FILENAME = "NUTS-1-EU.json";

	public static File getWpNuts1File() {
		return new File(Config.get(Config.KEY_DOWNLOAD_DIRECTORY), WPNUTS1_FILENAME);
	}

	/**
	 * Downloads NUTS 1 sources from 17:45, 7 December 2022.
	 * 
	 * @see https://en.wikipedia.org/w/index.php?title=First-level_NUTS_of_the_European_Union&oldid=1126125069
	 * @see https://en.wikipedia.org/w/api.php?action=query&prop=revisions&titles=First-level_NUTS_of_the_European_Union&rvslots=*&rvprop=content
	 */
	public WikipediaLinking downloadWpNuts1() {
		Io.download(PREFIX_WP_OLDID + WPNUTS1_OLDID, getWpNuts1File(), false);
		return this;
	}

	public String getWpNuts1Markdown() {
		return new JSONObject(Io.readFileToString(getWpNuts1File())).getJSONObject("parse").getJSONObject("wikitext")
				.getString("*");
	}

	public Map<String, String> getWpNuts1CodesToPages(String markdown) {
		Map<String, String> codeToPage = new LinkedHashMap<>();

		if (Boolean.FALSE)
			System.out.println(markdown);

		// e.g. "! style="background: #f8d8a8;" align="center"| '''AT1'''"
		Pattern patternNuts1 = Pattern.compile("'''(.*)'''");

		// e.g. "! [[East Austria]]"
		// e.g. "! [[Brussels|Brussels Capital Region]]"
		Pattern patternNuts1Link = Pattern.compile("\\[\\[(.*)\\]\\]");

		// e.g. "! style="background: #dcdcdc"| [[NUTS of Austria|AT]] !! colspan="6"
		// align="center"; style="background: #dcdcdc"| {{flag|Austria}} <span
		// id="Austria"></span>"
		Pattern patternNuts0 = Pattern.compile("NUTS of .*\\|([A-Z]+)\\]\\].*flag\\|(.*?)\\}");

		Matcher matcher;
		boolean nextIsLink = false;
		String code = null;
		String page = null;

		for (String line : markdown.split("\n")) {

			if (nextIsLink) {
				matcher = patternNuts1Link.matcher(line);
				if (matcher.find()) {
					page = matcher.group(1);
					if (page.contains("|"))
						page = page.substring(page.indexOf("|") + 1);
					codeToPage.put(code, page);
				}
				nextIsLink = false;
				code = null;
				page = null;
				continue;
			}

			matcher = patternNuts1.matcher(line);
			if (matcher.find()) {
				nextIsLink = true;
				code = matcher.group(1);
				continue;
			}

			matcher = patternNuts0.matcher(line);
			if (matcher.find()) {
				codeToPage.put(matcher.group(1), matcher.group(2));
				continue;
			}
		}

		return codeToPage;
	}

	public Map<String, String> getWpNuts1CodesToUris(String markdown) {
		Map<String, String> codeToLink = new LinkedHashMap<>();
		for (Entry<String, String> entry : getWpNuts1CodesToPages(markdown).entrySet()) {
			codeToLink.put(entry.getKey(), "https://en.wikipedia.org/wiki/" + entry.getValue().replace(" ", "_"));
		}
		return codeToLink;
	}
}