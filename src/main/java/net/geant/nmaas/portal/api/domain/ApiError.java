package net.geant.nmaas.portal.api.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@NoArgsConstructor(access = AccessLevel.PUBLIC)
@Getter
@Setter
public class ApiError {
	private String message;
	long timestamp;
	String statusMessage;
	int statusCode;
	
	public ApiError(String message, long timestamp, HttpStatus status) {
		super();
		this.message = message;
		this.timestamp = timestamp;
		if(status != null) {
			this.statusMessage = status.getReasonPhrase();
			this.statusCode = status.value();
		}
	}
	
}
