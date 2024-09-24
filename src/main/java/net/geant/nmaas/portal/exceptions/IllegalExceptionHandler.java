package net.geant.nmaas.portal.exceptions;

import net.bytebuddy.pool.TypePool;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class IllegalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handle(IllegalArgumentException ex) {
        throw new ObjectAlreadyExistsException(ex.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<?> handle(IllegalStateException ex) {
        throw new ObjectAlreadyExistsException(ex.getMessage());
    }
}
