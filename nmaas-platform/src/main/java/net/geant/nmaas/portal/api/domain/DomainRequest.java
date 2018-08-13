package net.geant.nmaas.portal.api.domain;

import javax.validation.constraints.NotNull;

public class DomainRequest {

	@NotNull
	private String name;

	@NotNull
	private String codename;

	private boolean dcnConfigured;

	private String kubernetesNamespace;

	private String kubernetesStorageClass;
	
	private boolean active = true;
	
	public DomainRequest() {
		super();
	}

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
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCodename() {
		return codename;
	}

	public void setCodename(String codename) {
		this.codename = codename;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public boolean isDcnConfigured() {
		return dcnConfigured;
	}

	public void setDcnConfigured(boolean dcnConfigured) {
		this.dcnConfigured = dcnConfigured;
	}

	public String getKubernetesNamespace() {
		return kubernetesNamespace;
	}

	public void setKubernetesNamespace(String kubernetesNamespace) {
		this.kubernetesNamespace = kubernetesNamespace;
	}

	public String getKubernetesStorageClass() {
		return kubernetesStorageClass;
	}

	public void setKubernetesStorageClass(String kubernetesStorageClass) {
		this.kubernetesStorageClass = kubernetesStorageClass;
	}
}
