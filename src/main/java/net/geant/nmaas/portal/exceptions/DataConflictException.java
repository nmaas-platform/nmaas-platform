package net.geant.nmaas.portal.exceptions;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DataConflictException extends RuntimeException {

    private String message;
    public DataConflictException(String message)
    {
        super(message);
        log.error("test ?????");
        this.message = message;
    }

    public String getMsg() {
        return message;
    }

}
