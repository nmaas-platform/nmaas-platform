package net.geant.nmaas.portal.api.exceptions;

public class SignupException extends PortalException {

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
