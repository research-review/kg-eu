package org.review;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.review.sources.Source;
import org.review.sources.Sources;

/**
 * Tests {@link Sources}.
 *
 * @author 33a1cc8d616a72f953d8e15274194bcd5aac2b78fbe6b4a4d1a911e0f2ef00cd
 */
class SourcesTest {

	@Test
	void test() throws IOException {

		// Parse without exceptions
		List<Source> sources = new Sources().getSources();
		Assertions.assertTrue(sources.size() >= 7 + 14 + 1);

		// For humans
		if (Boolean.FALSE) {
			for (Source source : sources) {
				System.out.println(source);
			}
		}
		if (Boolean.FALSE) {
			for (Source source : sources) {
				System.out.println(source.id);
				System.out.println(source.fileType);
				System.out.println(source.sourceType);
				System.out.println(source.sources);
				System.out.println();
			}
		}
	}
}