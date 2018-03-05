package net.geant.nmaas.portal.api.domain;

public class ApplicationSubscription extends ApplicationSubscriptionBase {

	protected boolean active = false;
	
	public ApplicationSubscription(Long domainId, Long applicationId) {
		super(domainId, applicationId);
	}

	protected boolean isActive() {
		return active;
	}

	protected void setActive(boolean active) {
		this.active = active;
	}

	
	
}
