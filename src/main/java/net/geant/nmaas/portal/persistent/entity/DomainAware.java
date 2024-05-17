package net.geant.nmaas.portal.persistent.entity;

import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

@MappedSuperclass
@Getter
@Setter
public class DomainAware {
	
	@ManyToOne(fetch=FetchType.LAZY, cascade = {}, optional = false)
	protected Domain domain;
	
}
