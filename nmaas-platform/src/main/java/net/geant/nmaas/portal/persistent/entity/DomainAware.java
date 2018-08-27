package net.geant.nmaas.portal.persistent.entity;

import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

@MappedSuperclass
@Getter
@Setter
public class DomainAware {
	
	@ManyToOne(fetch=FetchType.LAZY, cascade = {}, optional = false)
	protected Domain domain;
	
}
