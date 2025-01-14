package net.geant.nmaas.portal.exceptions;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DataConflictException extends RuntimeException {

    public DataConflictException(String message) {
        super(message);
    }

}
