package net.geant.nmaas.portal.api.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
public class ApplicationSubscription extends ApplicationSubscriptionBase {

	protected boolean active = false;

	public ApplicationSubscription(Long domainId, Long applicationId) {
		super(domainId, applicationId);
	}

}
