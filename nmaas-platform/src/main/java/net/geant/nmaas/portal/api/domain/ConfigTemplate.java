package net.geant.nmaas.portal.api.domain;

import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.JsonNode;

public class ConfigTemplate {
	
	public ConfigTemplate() {
		super();
	}

	public ConfigTemplate(String template) {
		super();
		this.template = template;
	}

	@JsonRawValue
	String template;

	public String getTemplate() {
		return template;
	}

	public void setTemplate(String template) {
		this.template = template;
	}

	@JsonSetter("template")
	public void setRawTemplate(JsonNode json) {
		this.template = json.toString();
	}
	
}
