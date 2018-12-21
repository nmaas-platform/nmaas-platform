package net.geant.nmaas.portal.api.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class AppInstance extends DomainAware {

	private Long id;
	
	private Long applicationId;

	private String applicationName;

	private String name;
	
	private Long createdAt;
	
	private User owner;
	
	private String configuration;

	private ConfigTemplate wizard;
	
	private AppInstanceState state;

	private String userFriendlyState;
	
	private String url;

}
