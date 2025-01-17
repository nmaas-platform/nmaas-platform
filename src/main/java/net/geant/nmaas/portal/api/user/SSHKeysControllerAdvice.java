package net.geant.nmaas.portal.api.user;

import net.geant.nmaas.portal.api.exception.ValidationError;
import net.geant.nmaas.portal.api.exception.ValidationErrorBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice(assignableTypes = SSHKeysController.class)
public class SSHKeysControllerAdvice extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        ValidationError error = ValidationErrorBuilder.fromBindingErrors(ex.getBindingResult());
        return super.handleExceptionInternal(ex, error, headers, status, request);
    }
}
