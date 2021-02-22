package net.geant.nmaas.portal.api.domain;

import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class DomainRequest {

	@NotNull
	private String name;

	@NotNull
	private String codename;

	private DomainDcnDetailsView domainDcnDetails = new DomainDcnDetailsView();

	private DomainTechDetailsView domainTechDetails = new DomainTechDetailsView();
	
	private boolean active = true;

	public DomainRequest(String name, String codename, boolean active){
		this.name = name;
		this.codename = codename;
		this.active = active;
	}
}
