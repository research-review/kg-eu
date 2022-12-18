package org.review.exceptions;

public class CommandRuntimeException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	public Integer exitValue = null;

	public CommandRuntimeException() {
		super();
	}

	public CommandRuntimeException(String message) {
		super(message);
	}

	public CommandRuntimeException(String message, int exitValue) {
		super(message);
		this.exitValue = exitValue;
	}

	public CommandRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public CommandRuntimeException(Throwable cause) {
		super(cause);
	}

	protected CommandRuntimeException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}