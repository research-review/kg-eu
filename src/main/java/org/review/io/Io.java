package org.review.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.review.exceptions.DownloadRuntimeException;
import org.review.exceptions.IoRuntimeException;

/**
 * General input / output.
 *
 * @author 33a1cc8d616a72f953d8e15274194bcd5aac2b78fbe6b4a4d1a911e0f2ef00cd
 */
public abstract class Io {

	/**
	 * Gets unsorted list of directory contents (names of directories and files).
	 */
	public static List<URI> listDirectory(String directory) {
		try {
			return Files.list(Paths.get(directory)).map(Path::toUri).collect(Collectors.toList());
		} catch (IOException e) {
			throw new IoRuntimeException(e);
		}
	}

	/**
	 * Gets sorted set of file URIs.
	 */
	public static SortedSet<URI> getFileUris(String directory) {
		SortedSet<URI> set = new TreeSet<>();
		for (URI uri : Io.listDirectory(directory)) {
			set.add(uri);
		}
		return set;
	}

	/**
	 * Throws runtime exception, if file is not readable.
	 */
	public static void checkReadable(File file) {
		if (file == null) {
			throw new IoRuntimeException("File not set.");
		} else if (!file.canRead()) {
			throw new IoRuntimeException("Can not read " + file.getAbsolutePath());
		}
	}

	/**
	 * Returns URL of file.
	 */
	public static URL fileToUrl(File file) {
		return uriToUrl(file.toURI());
	}

	/**
	 * Returns URL of URI.
	 */
	public static URL uriToUrl(URI uri) {
		try {
			return uri.toURL();
		} catch (MalformedURLException e) {
			throw new IoRuntimeException(e);
		}
	}

	/**
	 * Returns URI of URL.
	 */
	public static URI urlToUri(URL url) {
		try {
			return url.toURI();
		} catch (URISyntaxException e) {
			throw new IoRuntimeException(e);
		}
	}

	/**
	 * Writes string to file.
	 */
	public static void writeStringToFile(String string, File file) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(file));
		writer.write(string);
		writer.close();
	}

	/**
	 * Reads string to file.
	 */
	public static String readFileToString(File file) {
		try {
			return Files.readString(file.toPath());
		} catch (IOException e) {
			throw new IoRuntimeException(e);
		}
	}

	/**
	 * Downloads file.
	 * 
	 * @param url   URL to download
	 * @param file  File to save
	 * @param force True if existing files should be overwritten
	 * 
	 * @throws DownloadRuntimeException On any exceptions during download.
	 */
	public static void download(String urlString, File file, boolean force) {
		try {
			if (!file.exists() || force) {
				URL url = new URL(urlString);
				System.out.println("Downloading file " + file.getAbsolutePath() + " from " + url);
				file.getParentFile().mkdirs();
				// Source: https://www.baeldung.com/java-download-file
				ReadableByteChannel readableByteChannel = Channels.newChannel(url.openStream());
				FileOutputStream fileOutputStream = new FileOutputStream(file);
				fileOutputStream.getChannel().transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
				fileOutputStream.close();
			} else if (file.exists()) {
				System.out.println("Skipping download of existing file: " + file.getAbsolutePath());
			}
		} catch (IOException e) {
			throw new DownloadRuntimeException(e, urlString, file);
		}

	}
}