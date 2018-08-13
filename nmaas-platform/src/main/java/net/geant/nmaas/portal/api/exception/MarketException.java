package net.geant.nmaas.portal.api.exception;

public class MarketException extends RuntimeException {

	public MarketException() {
		super();
	}

	public MarketException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public MarketException(String message, Throwable cause) {
		super(message, cause);
	}

	public MarketException(String message) {
		super(message);
	}

	public MarketException(Throwable cause) {
		super(cause);
	}

}
