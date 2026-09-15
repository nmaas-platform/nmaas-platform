package net.geant.nmaas.portal.api.security.exceptions;

public class JwtInitializationException extends RuntimeException {

    public JwtInitializationException(String message) {
        super(message);
    }

    public JwtInitializationException(String message, Throwable cause) {
        super(message, cause);
    }

    public JwtInitializationException(Throwable cause) {
        super(cause);
    }
}
