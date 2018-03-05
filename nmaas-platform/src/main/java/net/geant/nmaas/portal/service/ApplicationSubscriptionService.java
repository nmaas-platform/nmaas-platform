package net.geant.nmaas.portal.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import net.geant.nmaas.portal.exceptions.ProcessingException;
import net.geant.nmaas.portal.exceptions.ObjectAlreadyExistsException;
import net.geant.nmaas.portal.exceptions.ObjectNotFoundException;
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
	
	Page<ApplicationSubscription> getSubscriptions(Pageable pageable);
	List<ApplicationSubscription> getSubscriptions();

	List<ApplicationSubscription> getSubscriptionsBy(Long domainId, Long applicationId);
	Page<ApplicationSubscription> getSubscriptionsBy(Long domainId, Long applicationId, Pageable pageable);

	List<ApplicationSubscription> getSubscriptionsBy(Domain domain, Application application);	
	Page<ApplicationSubscription> getSubscriptionsBy(Domain domain, Application application, Pageable pageable);
	
	ApplicationSubscription subscribe(ApplicationSubscription appSub) throws ObjectAlreadyExistsException, ProcessingException;
	ApplicationSubscription subscribe(Long applicationId, Long domainId, boolean active) throws ObjectAlreadyExistsException, ProcessingException, ObjectNotFoundException;
	ApplicationSubscription subscribe(Application application, Domain domain, boolean active) throws ObjectAlreadyExistsException, ProcessingException;
	
	boolean unsubscribe(ApplicationSubscription appSub) throws ProcessingException, ObjectNotFoundException;
	boolean unsubscribe(Long applicationId, Long domainId) throws ProcessingException, ObjectNotFoundException;
	boolean unsubscribe(Application application, Domain domain) throws ProcessingException, ObjectNotFoundException;
		
}
