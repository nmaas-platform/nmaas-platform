package net.geant.nmaas.portal.api.domain;

public abstract class DomainAware {

	Long domainId;

	public Long getDomainId() {
		return domainId;
	}

	public void setDomainId(Long domainId) {
		this.domainId = domainId;
	}
	
}
