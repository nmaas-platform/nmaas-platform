package net.geant.nmaas.portal.persistent.entity;

import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public class DomainAware {
	
	@ManyToOne(fetch=FetchType.LAZY, cascade = {}, optional = false)
	protected Domain domain;

	public Domain getDomain() {
		return domain;
	}

	public void setDomain(Domain domain) {
		this.domain = domain;
	}
	
}
