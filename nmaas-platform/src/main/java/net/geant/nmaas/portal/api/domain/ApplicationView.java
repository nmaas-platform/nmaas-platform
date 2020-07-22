package net.geant.nmaas.portal.api.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * full Application & ApplicationBase DTO
 */
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class ApplicationView extends ApplicationBriefView {

	Long appVersionId;
	String version;
	ConfigWizardTemplateView configWizardTemplate;
	ConfigWizardTemplateView configUpdateWizardTemplate;
	AppDeploymentSpecView appDeploymentSpec;
	AppConfigurationSpecView appConfigurationSpec;
	
}
