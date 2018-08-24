package net.geant.nmaas.portal.api.domain;

import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class DomainRequest {

	@NotNull
	private String name;

	@NotNull
	private String codename;

	private boolean dcnConfigured;

	private String kubernetesNamespace;

	private String kubernetesStorageClass;
	
	private boolean active = true;

	public DomainRequest(String name, String codename) {
		super();
		this.name = name;
		this.codename = codename;
	}

	public DomainRequest(String name, String codename, boolean active) {
		super();
		this.name = name;
		this.codename = codename;
		this.active = active;
	}
}
