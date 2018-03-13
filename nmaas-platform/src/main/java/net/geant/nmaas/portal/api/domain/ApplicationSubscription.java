package net.geant.nmaas.portal.api.domain;

public class ApplicationSubscription extends ApplicationSubscriptionBase {

	protected boolean active = false;
	
	protected ApplicationSubscription() {
		super();
	}
	
	public ApplicationSubscription(Long domainId, Long applicationId) {
		super(domainId, applicationId);
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	
	
}
