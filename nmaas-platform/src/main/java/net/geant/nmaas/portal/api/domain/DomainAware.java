package net.geant.nmaas.portal.api.domain;

import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class DomainAware {

	@NotNull
	Long domainId;
	
}
