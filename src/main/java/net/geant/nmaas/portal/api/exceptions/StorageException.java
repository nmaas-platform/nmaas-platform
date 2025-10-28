package net.geant.nmaas.portal.api.exceptions;

public class StorageException extends PortalException {

	public StorageException() {
		super();
	}

	public StorageException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public StorageException(String message, Throwable cause) {
		super(message, cause);
	}

	public StorageException(String message) {
		super(message);
	}

	public StorageException(Throwable cause) {
		super(cause);
	}

}
