package net.geant.nmaas.portal.api.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import net.geant.nmaas.portal.persistent.entity.ApplicationState;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * full, tremendous, legacy Application & ApplicationBase DTO
 */
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class ApplicationMassiveView extends ApplicationBaseView {

	Long appVersionId; // application version property - id of Application entity

	// application properties

	String owner;
	ApplicationState state;

	String version;
	ConfigWizardTemplateView configWizardTemplate;
	ConfigWizardTemplateView configUpdateWizardTemplate;
	@Valid
	AppDeploymentSpecView appDeploymentSpec;
	@Valid
	AppConfigurationSpecView appConfigurationSpec;
	
}
