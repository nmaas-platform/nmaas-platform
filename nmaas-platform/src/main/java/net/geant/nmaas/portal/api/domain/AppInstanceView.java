package net.geant.nmaas.portal.api.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
	
	private String url;

	private ConfigWizardTemplateView configWizardTemplate;

	private String descriptiveDeploymentId;
}
