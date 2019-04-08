package net.geant.nmaas.portal.api.domain;

import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class ConfigWizardTemplateView {

	@JsonRawValue
	String template;

	@JsonSetter("template")
	public void setRawTemplate(JsonNode json) {
		this.template = json.toString();
	}
	
}
