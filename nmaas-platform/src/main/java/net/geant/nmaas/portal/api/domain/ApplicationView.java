package net.geant.nmaas.portal.api.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class ApplicationView extends ApplicationBriefView {

	String version;
	ConfigWizardTemplateView configWizardTemplate;
	ConfigWizardTemplateView configUpdateWizardTemplate;
	AppDeploymentSpec appDeploymentSpec;
	AppConfigurationSpecView appConfigurationSpec;
	
}
