package net.geant.nmaas.portal.api.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ApplicationView extends ApplicationBriefView {

	ConfigTemplate configTemplate;
	ConfigTemplate configurationUpdateTemplate;
	AppDeploymentSpec appDeploymentSpec;
	
}
