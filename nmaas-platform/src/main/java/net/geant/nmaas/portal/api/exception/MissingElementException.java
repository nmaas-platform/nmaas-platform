package net.geant.nmaas.portal.api.exception;

public class MissingElementException extends MarketException {

	public MissingElementException() {
		super();
	}

	public MissingElementException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public MissingElementException(String message, Throwable cause) {
		super(message, cause);
	}

	public MissingElementException(String message) {
		super(message);
	}

	public MissingElementException(Throwable cause) {
		super(cause);
	}

}
