package net.geant.nmaas.portal.api.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Application extends ApplicationBrief {

	ConfigTemplate configTemplate;
	ConfigTemplate configurationUpdateTemplate;
	AppDeploymentSpec appDeploymentSpec;
	
}
