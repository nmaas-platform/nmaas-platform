package net.geant.nmaas.portal.api.domain;

import javax.validation.constraints.NotNull;

public class DomainRequest {

	@NotNull
	private String name;

	public DomainRequest() {
		super();
	}

	public DomainRequest(String name) {
		super();
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	
}
