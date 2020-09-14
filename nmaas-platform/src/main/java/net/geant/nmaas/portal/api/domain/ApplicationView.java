package net.geant.nmaas.portal.api.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * full Application & ApplicationBase DTO
 */
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class ApplicationView extends ApplicationBriefView {

	Long appVersionId; // application version property - id of Application entity

	// application properties
	String version;
	ConfigWizardTemplateView configWizardTemplate;
	ConfigWizardTemplateView configUpdateWizardTemplate;
	@Valid
	AppDeploymentSpecView appDeploymentSpec;
	@Valid
	AppConfigurationSpecView appConfigurationSpec;
	
}
