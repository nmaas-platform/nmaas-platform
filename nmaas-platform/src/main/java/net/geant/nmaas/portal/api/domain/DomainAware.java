package net.geant.nmaas.portal.api.domain;

import javax.validation.constraints.NotNull;

public abstract class DomainAware {

	@NotNull
	Long domainId;

	public Long getDomainId() {
		return domainId;
	}

	public void setDomainId(Long domainId) {
		this.domainId = domainId;
	}
	
}
