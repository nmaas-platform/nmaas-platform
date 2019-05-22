package net.geant.nmaas.portal.exceptions;

public class ObjectAlreadyExistsException extends RuntimeException {

	public ObjectAlreadyExistsException() {
		super();
	}

	public ObjectAlreadyExistsException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ObjectAlreadyExistsException(String message, Throwable cause) {
		super(message, cause);
	}

	public ObjectAlreadyExistsException(String message) {
		super(message);
	}

	public ObjectAlreadyExistsException(Throwable cause) {
		super(cause);	
	}
}
