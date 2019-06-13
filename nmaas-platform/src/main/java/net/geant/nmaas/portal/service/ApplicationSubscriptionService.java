package net.geant.nmaas.portal.service;

import java.util.List;
import java.util.Optional;

import net.geant.nmaas.portal.persistent.entity.Application;
import net.geant.nmaas.portal.persistent.entity.ApplicationSubscription;
import net.geant.nmaas.portal.persistent.entity.Domain;

public interface ApplicationSubscriptionService {
	
	boolean isActive(ApplicationSubscription.Id id);
	boolean isActive(Long applicationId, Long domainId);
	boolean isActive(Application application, Domain domain);
	
	boolean existsSubscription(ApplicationSubscription.Id id);
	boolean existsSubscription(Long applicationId, Long domainId);
	boolean existsSubscription(Application application, Domain domain);
	
	Optional<ApplicationSubscription> getSubscription(ApplicationSubscription.Id id);
	Optional<ApplicationSubscription> getSubscription(Long applicationId, Long domainId);
	Optional<ApplicationSubscription> getSubscription(Application application, Domain domain);
	
	List<ApplicationSubscription> getSubscriptions();

	List<ApplicationSubscription> getSubscriptionsBy(Long domainId, Long applicationId);

	List<ApplicationSubscription> getSubscriptionsBy(Domain domain, Application application);	

	ApplicationSubscription subscribe(ApplicationSubscription appSub);
	ApplicationSubscription subscribe(Long applicationId, Long domainId, boolean active);
	ApplicationSubscription subscribe(Application application, Domain domain, boolean active);
	
	void unsubscribe(ApplicationSubscription appSub);
	void unsubscribe(Long applicationId, Long domainId);
	void unsubscribe(Application application, Domain domain);
		
	List<Application> getSubscribedApplications();
	List<Application> getSubscribedApplications(Long domainId);
	
}
