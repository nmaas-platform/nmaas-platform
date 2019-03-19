package net.geant.nmaas.portal.api.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ApplicationView extends ApplicationBriefView {

	ConfigWizardTemplateView configTemplate;
	ConfigWizardTemplateView configurationUpdateTemplate;
	AppDeploymentSpec appDeploymentSpec;
	AppConfigurationSpecView appConfigurationSpec;
	
}
