package net.geant.nmaas.portal.api.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApiResponse {
	boolean success;
	
	public ApiResponse(boolean success) {
		super();
		this.success = success;
	}
}
