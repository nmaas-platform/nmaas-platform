package net.geant.nmaas.portal.api.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class AppInstanceStatus {
	private Long appInstanceId;
	private AppInstanceState state;
	private AppInstanceState previousState;
	private String details;
	private String userFriendlyDetails;
	private String userFriendlyState;
}
