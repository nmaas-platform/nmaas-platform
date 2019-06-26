package net.geant.nmaas.portal.service;

import java.util.List;
import java.util.Optional;

import net.geant.nmaas.portal.persistent.entity.ApplicationBase;
import net.geant.nmaas.portal.persistent.entity.ApplicationSubscription;
import net.geant.nmaas.portal.persistent.entity.Domain;

public interface ApplicationSubscriptionService {
	
	boolean isActive(ApplicationSubscription.Id id);
	boolean isActive(Long applicationId, Long domainId);
	boolean isActive(String appName, Domain domain);
	
	boolean existsSubscription(ApplicationSubscription.Id id);
	boolean existsSubscription(Long applicationId, Long domainId);
	boolean existsSubscription(ApplicationBase application, Domain domain);
	
	Optional<ApplicationSubscription> getSubscription(ApplicationSubscription.Id id);
	Optional<ApplicationSubscription> getSubscription(Long applicationId, Long domainId);
	Optional<ApplicationSubscription> getSubscription(ApplicationBase application, Domain domain);

	List<ApplicationSubscription> getSubscriptions();

	List<ApplicationSubscription> getSubscriptionsBy(Long domainId, Long applicationId);

	List<ApplicationSubscription> getSubscriptionsBy(Domain domain, Application application);	

	List<ApplicationSubscription> getSubscriptionsBy(Domain domain, ApplicationBase application);

	ApplicationSubscription subscribe(ApplicationSubscription appSub);
	ApplicationSubscription subscribe(Long applicationId, Long domainId, boolean active);
	ApplicationSubscription subscribe(ApplicationBase application, Domain domain, boolean active);
	
	void unsubscribe(ApplicationSubscription appSub);
	void unsubscribe(Long applicationId, Long domainId);
	void unsubscribe(ApplicationBase application, Domain domain);
		
	List<ApplicationBase> getSubscribedApplications();
	List<ApplicationBase> getSubscribedApplications(Long domainId);
	
}
