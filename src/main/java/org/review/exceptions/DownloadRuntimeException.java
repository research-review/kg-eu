package org.review.exceptions;

import java.io.File;

public class DownloadRuntimeException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public File file;
	public String url;

	public DownloadRuntimeException() {
		super();
	}

	public DownloadRuntimeException(String message) {
		super(message);
	}

	public DownloadRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public DownloadRuntimeException(Throwable cause) {
		super(cause);
	}

	public DownloadRuntimeException(Throwable cause, String url, File file) {
		super(cause);
		this.url = url;
		this.file = file;
	}

	protected DownloadRuntimeException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}