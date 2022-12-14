package net.geant.nmaas.portal.exceptions;

public class ApplicationSubscriptionNotActiveException extends RuntimeException {

	public ApplicationSubscriptionNotActiveException() {
		super();
	}

	public ApplicationSubscriptionNotActiveException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ApplicationSubscriptionNotActiveException(String message, Throwable cause) {
		super(message, cause);
	}

	public ApplicationSubscriptionNotActiveException(String message) {
		super(message);
	}

	public ApplicationSubscriptionNotActiveException(Throwable cause) {
		super(cause);
	}

}
