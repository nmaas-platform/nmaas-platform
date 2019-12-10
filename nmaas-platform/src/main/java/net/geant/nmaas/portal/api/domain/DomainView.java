package net.geant.nmaas.portal.api.domain;

import lombok.Getter;
import lombok.Setter;
import net.geant.nmaas.portal.persistent.entity.ApplicationStatePerDomain;

import java.util.List;

@Getter
@Setter
public class DomainView {
	Long id;

	String name;
	String codename;
	boolean active;
	DomainTechDetailsView domainTechDetails;
	DomainDcnDetailsView domainDcnDetails;

	List<ApplicationStatePerDomainView> applicationStatePerDomain;
}
