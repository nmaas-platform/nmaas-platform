package net.geant.nmaas.portal.persistent.entity;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;

public class DomainAware {
	
	@OneToOne(fetch=FetchType.LAZY, cascade = {}, optional = false)
	@Column(nullable = false)
	protected Domain domain;

	public Domain getDomain() {
		return domain;
	}

	public void setDomain(Domain domain) {
		this.domain = domain;
	}
	
}
