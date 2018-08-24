package net.geant.nmaas.portal.api.domain;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AppInstanceStatus {
	Long appInstanceId;
	AppInstanceState state;
	String details;
	String userFriendlyDetails;
	String userFriendlyState;
}
