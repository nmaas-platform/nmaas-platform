package net.geant.nmaas.portal.api.domain;

public enum AppInstanceState {
	SUBSCRIBED, 
	VALIDATION,
	PREPARATION,
	CONNECTING,
	CONFIGURATION_AWAITING,
	DEPLOYING,
	RUNNING,
	UNDEPLOYING,
	DONE,
	FAILURE 
	
}
