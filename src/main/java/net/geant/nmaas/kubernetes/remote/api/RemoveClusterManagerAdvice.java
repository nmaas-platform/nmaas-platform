package net.geant.nmaas.kubernetes.remote.api;

import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.api.dto.ApiError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.NoSuchElementException;

@RestControllerAdvice(assignableTypes = RemoteClusterManagerController.class)
@Slf4j
public class RemoveClusterManagerAdvice {

    @ExceptionHandler(value = {IllegalArgumentException.class})
    public ResponseEntity<ApiError> handleValidationException(IllegalArgumentException ex) {
        log.warn("Responding with 400 with errors: {}", ex.getMessage(), ex);
        long timestamp = System.currentTimeMillis();
        ApiError error = new ApiError(ex.getMessage(), timestamp, HttpStatus.BAD_REQUEST.getReasonPhrase(), HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(value = {NoSuchElementException.class})
    public ResponseEntity<ApiError> handleNotFoundException(NoSuchElementException ex) {
        long timestamp = System.currentTimeMillis();
        ApiError error = new ApiError(ex.getMessage(), timestamp, HttpStatus.NOT_FOUND.getReasonPhrase(), HttpStatus.NOT_FOUND.value());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

}
