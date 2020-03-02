package net.geant.nmaas.portal.api.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ServiceAccessMethodView;

import java.util.Set;

@NoArgsConstructor
@Getter
@Setter
public class AppInstanceView extends AppInstanceBase {

	private Long id;
	
	private Long applicationId;
	
	private Long createdAt;
	
	private UserView owner;
	
	private String configuration;
	
	private AppInstanceState state;

	private String userFriendlyState;

	private Set<ServiceAccessMethodView> serviceAccessMethods;

	private ConfigWizardTemplateView configWizardTemplate;

	private String descriptiveDeploymentId;
}
