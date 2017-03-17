package net.geant.nmaas.portal.api.domain;

import org.springframework.http.HttpStatus;

public class ApiError {
	private String message;
	long timestamp;
	String statusMessage;
	int statusCode;
	
	public ApiError() {
		
	}
	
	public ApiError(String message, long timestamp, HttpStatus status) {
		super();
		this.message = message;
		this.timestamp = timestamp;
		if(status != null) {
			this.statusMessage = status.getReasonPhrase();
			this.statusCode = status.value();
		}
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public String getStatusMessage() {
		return statusMessage;
	}

	public void setStatusMessage(String statusMessage) {
		this.statusMessage = statusMessage;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}
	
}
