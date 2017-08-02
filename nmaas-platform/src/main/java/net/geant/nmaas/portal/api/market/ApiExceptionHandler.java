package net.geant.nmaas.portal.api.market;

import net.geant.nmaas.portal.api.domain.ApiError;
import net.geant.nmaas.portal.api.exception.*;
import net.geant.nmaas.portal.api.security.exceptions.AuthenticationMethodNotSupportedException;
import net.geant.nmaas.portal.api.security.exceptions.BasicAuthenticationException;
import net.geant.nmaas.portal.api.security.exceptions.MissingTokenException;
import net.geant.nmaas.portal.api.security.exceptions.TokenAuthenticationException;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {
	final static Logger log = LogManager.getLogger(ApiExceptionHandler.class);
	
	@ExceptionHandler(value = { AuthenticationException.class,
								BasicAuthenticationException.class,
								AuthenticationMethodNotSupportedException.class,
								MissingTokenException.class,
								TokenAuthenticationException.class,
			                    AccessDeniedException.class })
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	public ApiError handleAuthenticationException(WebRequest req, Exception ex) {
		return createApiError(ex, HttpStatus.UNAUTHORIZED);
	}
	
	@ExceptionHandler(value = { MissingElementException.class })
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ApiError handleMissingElementException(WebRequest req, MarketException ex) {
		return createApiError(ex, HttpStatus.NOT_FOUND);
	}
	
	@ExceptionHandler(value = { SignupException.class })
	@ResponseStatus(HttpStatus.CONFLICT)
	public ApiError handleSignupException(WebRequest req, MarketException ex) {
		return createApiError(ex, HttpStatus.CONFLICT);
	}
	
	@ExceptionHandler(value = { ProcessingException.class })
	@ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
	public ApiError handleProcessingException(WebRequest req, MarketException ex) {
		return createApiError(ex, HttpStatus.NOT_ACCEPTABLE);
	}

	@ExceptionHandler(value = { StorageException.class })
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ApiError handleStorageException(WebRequest req, MarketException ex) {
		return createApiError(ex, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler(value = { MarketException.class })
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ApiError handleMarketException(WebRequest req, MarketException ex) {
		return createApiError(ex, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler(value = { Exception.class })
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ApiError handleException(WebRequest req, Exception ex) {
		return createApiError(ex, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	private ApiError createApiError(Exception ex, HttpStatus status) {
		long timestamp = System.currentTimeMillis();
		log.error("Error reported at " + timestamp, ex);
		return new ApiError(ex.getMessage(), timestamp, status);
	}

}
