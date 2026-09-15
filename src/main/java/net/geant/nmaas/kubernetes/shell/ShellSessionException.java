package net.geant.nmaas.kubernetes.shell;

public class ShellSessionException extends RuntimeException {

    public ShellSessionException(String message) {
        super(message);
    }

    public ShellSessionException(String message, Throwable cause) {
        super(message, cause);
    }
}
