package net.geant.nmaas.utils.ssh;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class CommandExecutionException extends RuntimeException {

    public CommandExecutionException(String message) {
        super(message);
    }

    public CommandExecutionException(String message, Throwable cause) {
        super(message, cause);
    }

}
