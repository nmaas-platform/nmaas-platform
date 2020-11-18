package net.geant.nmaas.portal.api.market;

import io.jsonwebtoken.ExpiredJwtException;
import lombok.extern.log4j.Log4j2;
import net.geant.nmaas.nmservice.configuration.exceptions.InvalidWebhookException;
import net.geant.nmaas.portal.api.domain.ApiError;
import net.geant.nmaas.portal.api.exception.AuthenticationException;
import net.geant.nmaas.portal.api.exception.MarketException;
import net.geant.nmaas.portal.api.exception.MissingElementException;
import net.geant.nmaas.portal.api.exception.ProcessingException;
import net.geant.nmaas.portal.api.exception.SignupException;
import net.geant.nmaas.portal.api.exception.StorageException;
import net.geant.nmaas.portal.api.security.exceptions.AuthenticationMethodNotSupportedException;
import net.geant.nmaas.portal.api.security.exceptions.BasicAuthenticationException;
import net.geant.nmaas.portal.api.security.exceptions.MissingTokenException;
import net.geant.nmaas.portal.api.security.exceptions.TokenAuthenticationException;
import net.geant.nmaas.portal.exceptions.UndergoingMaintenanceException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@RestControllerAdvice
@Log4j2
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

	@ExceptionHandler(value = { AuthenticationException.class,
								BasicAuthenticationException.class,
								AuthenticationMethodNotSupportedException.class,
								MissingTokenException.class,
								TokenAuthenticationException.class,
			                    AccessDeniedException.class,
								ExpiredJwtException.class})
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	public ApiError handleAuthenticationException(WebRequest req, Exception ex) {
		return createApiError(ex, HttpStatus.UNAUTHORIZED);
	}
	
	@ExceptionHandler(value = { MissingElementException.class, InvalidWebhookException.class})
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ApiError handleMissingElementException(WebRequest req, Exception ex) {
		return createApiError(ex, HttpStatus.NOT_FOUND);
	}
	
	@ExceptionHandler(value = { SignupException.class })
	@ResponseStatus(HttpStatus.CONFLICT)
	public ApiError handleSignupException(WebRequest req, MarketException ex) {
		return createApiError(ex, HttpStatus.CONFLICT);
	}
	
	@ExceptionHandler(value = { ProcessingException.class, UndergoingMaintenanceException.class})
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
		return createApiErrorAndLogStacktrace(ex, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler(value = { Exception.class })
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ApiError handleException(WebRequest req, Exception ex) {
		return createApiErrorAndLogStacktrace(ex, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler(IOException.class)
	@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
	public ApiError exceptionHandler(IOException e, HttpServletRequest request) {
		if (StringUtils.containsIgnoreCase(ExceptionUtils.getRootCauseMessage(e), "Broken pipe")) {
			log.debug("Detected `Broken pipe` IOException after executing: " + request.getRequestURL().toString());
			return null;
		} else {
			return createApiError(e, HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	private ApiError createApiErrorAndLogStacktrace(Exception ex, HttpStatus status) {
		long timestamp = System.currentTimeMillis();
		log.error("Error reported at " + timestamp, ex);
		return new ApiError(ex.getMessage(), timestamp, status);
	}

	private ApiError createApiError(Exception ex, HttpStatus status){
		long timestamp = System.currentTimeMillis();
		return new ApiError(ex.getMessage(), timestamp, status);
	}

}
