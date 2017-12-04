package net.geant.nmaas.portal.exceptions;

public class PortalException extends Exception {

	public PortalException() {
		super();
	}

	public PortalException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public PortalException(String message, Throwable cause) {
		super(message, cause);
	}

	public PortalException(String message) {
		super(message);
	}

	public PortalException(Throwable cause) {
		super(cause);
	}

}
