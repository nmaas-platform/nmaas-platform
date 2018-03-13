package net.geant.nmaas.portal.api.domain;

public class ApplicationSubscriptionBase {

	protected Long domainId;
	protected Long applicationId;

	protected ApplicationSubscriptionBase() {
		super();
	}
	
	public ApplicationSubscriptionBase(Long domainId, Long applicationId) {
		super();
		this.domainId = domainId;
		this.applicationId = applicationId;
	}

	public void setDomainId(Long domainId) {
		this.domainId = domainId;
	}

	public void setApplicationId(Long applicationId) {
		this.applicationId = applicationId;
	}

	public Long getDomainId() {
		return domainId;
	}
	public Long getApplicationId() {
		return applicationId;
	}
	
}
