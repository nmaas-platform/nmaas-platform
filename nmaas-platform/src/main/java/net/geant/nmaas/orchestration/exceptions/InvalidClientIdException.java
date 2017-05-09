package net.geant.nmaas.orchestration.exceptions;

import net.geant.nmaas.orchestration.entities.Identifier;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class InvalidClientIdException extends Exception {

    public InvalidClientIdException() {
        super();
    }

    public InvalidClientIdException(String message) {
        super(message);
    }

    public InvalidClientIdException(Identifier clientId) {
        super(clientId.value());
    }

}
