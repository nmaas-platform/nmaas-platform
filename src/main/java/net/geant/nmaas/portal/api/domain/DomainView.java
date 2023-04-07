package net.geant.nmaas.portal.api.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class DomainView extends DomainBase {

	List<ApplicationStatePerDomainView> applicationStatePerDomain;

	DomainTechDetailsView domainTechDetails;
	DomainDcnDetailsView domainDcnDetails;

	List<DomainGroupViewS> groups;

}
