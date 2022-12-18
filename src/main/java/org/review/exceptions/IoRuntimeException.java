package org.review.exceptions;

public class IoRuntimeException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public IoRuntimeException() {
		super();
	}

	public IoRuntimeException(String message) {
		super(message);
	}

	public IoRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public IoRuntimeException(Throwable cause) {
		super(cause);
	}

	protected IoRuntimeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}