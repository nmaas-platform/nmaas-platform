package net.geant.nmaas.portal.api.security.exceptions;

import org.springframework.security.core.AuthenticationException;

public class AuthenticationMethodNotSupportedException extends AuthenticationException {

	public AuthenticationMethodNotSupportedException(String msg) {
		super(msg);
	}

}
