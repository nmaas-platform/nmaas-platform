package net.geant.nmaas.portal.api.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DomainView extends DomainBase {

	DomainTechDetailsView domainTechDetails;
	DomainDcnDetailsView domainDcnDetails;


}
