package net.geant.nmaas.portal.exceptions;

public class PortalConfigurationException extends Exception {

	public PortalConfigurationException() {
		super();
	}

	public PortalConfigurationException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public PortalConfigurationException(String message, Throwable cause) {
		super(message, cause);
	}

	public PortalConfigurationException(String message) {
		super(message);
	}

	public PortalConfigurationException(Throwable cause) {
		super(cause);
	}

}
