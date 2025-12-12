package net.geant.nmaas.kubernetes.remote.api;

import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.portal.domain.ApiError;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.NoSuchElementException;

@ControllerAdvice(assignableTypes = RemoteClusterManagerController.class)
@Slf4j
public class RemoveClusterManagerAdvice {

    @ExceptionHandler(value = {NoSuchElementException.class, IllegalArgumentException.class})
    public ApiError handleValidationExceptions(NoSuchElementException ex) {
        log.warn("Responding with 400 with errors: {}", ex.getMessage());
        long timestamp = System.currentTimeMillis();
        return new ApiError(ex.getMessage(), timestamp, HttpStatus.BAD_REQUEST);
    }

}