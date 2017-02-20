package net.geant.nmaas.portal.api.security.exceptions;

import org.springframework.security.core.AuthenticationException;

public class BasicAuthenticationException extends AuthenticationException {

	public BasicAuthenticationException(String msg) {
		super(msg);
	}

	public BasicAuthenticationException(String msg, Throwable t) {
		super(msg, t);
	}

}
