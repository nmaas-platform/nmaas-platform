package net.geant.nmaas.portal.api.domain;

public class ConfigTemplate {
	
	public ConfigTemplate() {
		super();
	}

	public ConfigTemplate(String template) {
		super();
		this.template = template;
	}

	String template;

	public String getTemplate() {
		return template;
	}

	public void setTemplate(String template) {
		this.template = template;
	}
	
}
