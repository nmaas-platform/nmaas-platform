package net.geant.nmaas.portal.api.domain;

public class Domain {
	Long id;

	String name;
	String codename;
	boolean active;
	String kubernetesNamespace;
	boolean dcnConfigured;
	String persistentClass;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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

	public String getKubernetesNamespace() {
		return kubernetesNamespace;
	}

	public void setKubernetesNamespace(String kubernetesNamespace) {
		this.kubernetesNamespace = kubernetesNamespace;
	}

	public boolean isDcnConfigured() {
		return dcnConfigured;
	}

	public void setDcnConfigured(boolean dcnConfigured) {
		this.dcnConfigured = dcnConfigured;
	}

	public String getPersistentClass() {
		return persistentClass;
	}

	public void setPersistentClass(String persistentClass) {
		this.persistentClass = persistentClass;
	}
}
