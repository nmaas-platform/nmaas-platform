package net.geant.nmaas.portal.api.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Setter
public class ApplicationSubscriptionBase {

	protected Long domainId;
	protected Long applicationId;
	
}
