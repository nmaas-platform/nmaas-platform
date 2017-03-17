package net.geant.nmaas.portal.api.domain;

public class Application extends ApplicationBrief {

	String description;
	ConfigTemplate configTemplate;
	
	public Application() {
		super();
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public ConfigTemplate getConfigTemplate() {
		return configTemplate;
	}

	public void setConfigTemplate(ConfigTemplate configTemplate) {
		this.configTemplate = configTemplate;
	}
}
