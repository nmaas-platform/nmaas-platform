package net.geant.nmaas.portal.api.security.exceptions;

import org.springframework.security.core.AuthenticationException;

public class MissingTokenException extends AuthenticationException {

	public MissingTokenException(String msg) {
		super(msg);
	}

	public MissingTokenException(String msg, Throwable t) {
		super(msg, t);
	}

}
