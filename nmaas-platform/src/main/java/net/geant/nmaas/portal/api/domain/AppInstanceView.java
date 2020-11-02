package net.geant.nmaas.portal.api.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ServiceAccessMethodView;
import net.geant.nmaas.orchestration.AppConfigRepositoryAccessDetails;

import java.util.Set;

@NoArgsConstructor
@Getter
@Setter
public class AppInstanceView extends AppInstanceBase {
	
	private Long applicationId;
	
	private String configuration;

	private Set<ServiceAccessMethodView> serviceAccessMethods;

	private ConfigWizardTemplateView configWizardTemplate;

	private ConfigWizardTemplateView configUpdateWizardTemplate;

	private String descriptiveDeploymentId;

	private AppConfigRepositoryAccessDetails appConfigRepositoryAccessDetails;

	private Set<UserBase> members;
}
