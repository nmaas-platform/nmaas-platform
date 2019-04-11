package net.geant.nmaas.portal.api.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DomainView {
	Long id;

	String name;
	String codename;
	boolean active;
	DomainTechDetailsView domainTechDetails;
	DomainDcnDetailsView domainDcnDetails;
}
