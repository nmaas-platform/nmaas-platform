package net.geant.nmaas.portal.api.domain;

import lombok.AllArgsConstructor;
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

	private Set<UserViewMinimal> members;

	// application version to which this instance can be upgraded
	private AppInstanceUpgradeInfo upgradeInfo;

	@NoArgsConstructor
	@AllArgsConstructor
	@Getter
	@Setter
	public static class AppInstanceUpgradeInfo {

		private Long applicationId;

		private String applicationVersion;

		private String helmChartVersion;

	}

}