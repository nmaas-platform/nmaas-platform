package net.geant.nmaas.nmservice.configuration.exceptions;

public class GitRepositoryOperationException extends RuntimeException {

    public GitRepositoryOperationException(String message) {
        super(message);
    }

    public GitRepositoryOperationException(String message, Throwable cause) {
        super(message, cause);
    }

}
