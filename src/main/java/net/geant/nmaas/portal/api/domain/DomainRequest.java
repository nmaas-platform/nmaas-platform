package net.geant.nmaas.portal.api.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class DomainRequest {

	@NotNull
	private String name;

	@NotNull
	private String codename;

	@Builder.Default
	private DomainDcnDetailsView domainDcnDetails = new DomainDcnDetailsView();

	@Builder.Default
	private DomainTechDetailsView domainTechDetails = new DomainTechDetailsView();

	@Builder.Default
	private boolean active = true;

	public DomainRequest(String name, String codename, boolean active) {
		this.name = name;
		this.codename = codename;
		this.active = active;
	}

}
