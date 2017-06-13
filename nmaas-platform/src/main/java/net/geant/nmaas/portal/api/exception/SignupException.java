package net.geant.nmaas.portal.api.exception;

public class SignupException extends MarketException {

	public SignupException() {
		super();
	}

	public SignupException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public SignupException(String message, Throwable cause) {
		super(message, cause);
	}

	public SignupException(String message) {
		super(message);
	}

	public SignupException(Throwable cause) {
		super(cause);
	}

}
