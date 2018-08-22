package net.geant.nmaas.portal.service.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apache.tomcat.jni.Proc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import net.geant.nmaas.portal.exceptions.ProcessingException;
import net.geant.nmaas.portal.exceptions.ObjectAlreadyExistsException;
import net.geant.nmaas.portal.exceptions.ObjectNotFoundException;
import net.geant.nmaas.portal.persistent.entity.Application;
import net.geant.nmaas.portal.persistent.entity.ApplicationSubscription;
import net.geant.nmaas.portal.persistent.entity.ApplicationSubscription.Id;
import net.geant.nmaas.portal.persistent.entity.Domain;
import net.geant.nmaas.portal.persistent.repositories.ApplicationSubscriptionRepository;
import net.geant.nmaas.portal.service.DomainService;
import net.geant.nmaas.portal.service.ApplicationService;
import net.geant.nmaas.portal.service.ApplicationSubscriptionService;

@Service
public class ApplicationSubscriptionServiceImpl implements ApplicationSubscriptionService {

	@Autowired
    ApplicationSubscriptionRepository appSubRepo;
	
	@Autowired
	DomainService domains;
	
	@Autowired
	ApplicationService applications;
	
	
	@Override
	public boolean isActive(Id id) {
		boolean isActive = appSubRepo.findById(id)
				.map((appInstance) -> (!appInstance.isDeleted() && appInstance.isActive())).orElse(false);
		return isActive;
	}

	@Override
	public boolean isActive(Long applicationId, Long domainId) {
		boolean isActive = appSubRepo.findByDomainAndApplicationId(domainId, applicationId)
									.map((appInstance) -> (!appInstance.isDeleted() && appInstance.isActive()))
									.orElse(false);
		return isActive;
	}

	@Override
	public boolean isActive(Application application, Domain domain) {
		boolean isActive = appSubRepo.findByDomainAndApplication(domain, application)
				.map((appInstance) -> (!appInstance.isDeleted() && appInstance.isActive()))
				.orElse(false);
		return isActive;
	}

	@Override
	public boolean existsSubscription(Id id) {
		checkParam(id);
		return appSubRepo.existsById(id);
	}

	@Override
	public boolean existsSubscription(Long applicationId, Long domainId) {
		checkParam(applicationId, domainId);
		return appSubRepo.existsByDomainAndApplicationId(domainId, applicationId);
	}

	@Override
	public boolean existsSubscription(Application application, Domain domain) {
		checkParam(application, domain);
		return appSubRepo.existsByDomainAndApplication(domain, application);
	}

	@Override
	public Optional<ApplicationSubscription> getSubscription(Id id) {
		checkParam(id);
		return appSubRepo.findById(id);
	}

	@Override
	public Optional<ApplicationSubscription> getSubscription(Long applicationId, Long domainId) {
		checkParam(applicationId, domainId);
		return appSubRepo.findByDomainAndApplicationId(domainId, applicationId);
	}

	@Override
	public Optional<ApplicationSubscription> getSubscription(Application application, Domain domain) {
		checkParam(application, domain);
		return appSubRepo.findByDomainAndApplication(domain, application);
	}
	
	@Override
	public List<ApplicationSubscription> getSubscriptions() {
		return appSubRepo.findAll();
	}

	
	@Override
	public Page<ApplicationSubscription> getSubscriptions(Pageable pageable) {
		return appSubRepo.findAll(pageable);
	}

	@Override
	public List<ApplicationSubscription> getSubscriptionsBy(Long domainId, Long applicationId) {
		if(domainId == null && applicationId == null)
			return appSubRepo.findAll();
		else if(domainId != null)
			return appSubRepo.findAllByDomain(domainId);
		else if(applicationId != null)
			return appSubRepo.findAllByApplication(applicationId);
		else {
			Optional<ApplicationSubscription> res = appSubRepo.findByDomainAndApplicationId(domainId, applicationId);
			return Arrays.asList(res.orElse(null));
		}
	}

	@Override
	public Page<ApplicationSubscription> getSubscriptionsBy(Long domainId, Long applicationId, Pageable pageable) {
		if(domainId == null && applicationId == null)
			return appSubRepo.findAll(pageable);
		else if(domainId != null)
			return appSubRepo.findAllByDomain(domainId, pageable);
		else if(applicationId != null)
			return appSubRepo.findAllByApplication(applicationId, pageable);
		else {
			Optional<ApplicationSubscription> res = appSubRepo.findByDomainAndApplicationId(domainId, applicationId);
			return new PageImpl<ApplicationSubscription>(Arrays.asList(res.orElse(null)), 
														pageable, 
														res.isPresent() ? 1 : 0);
		}
	}

	@Override
	public List<ApplicationSubscription> getSubscriptionsBy(Domain domain, Application application) {
		if(domain == null && application == null)
			return appSubRepo.findAll();
		else if(domain != null)
			return appSubRepo.findAllByDomain(domain);
		else if(application != null)
			return appSubRepo.findAllByApplication(application);
		else {
			Optional<ApplicationSubscription> res = appSubRepo.findByDomainAndApplication(domain, application);
			return Arrays.asList(res.orElse(null));
		}		
	}

	@Override
	public Page<ApplicationSubscription> getSubscriptionsBy(Domain domain, Application application, Pageable pageable) {
		if(domain == null && application == null)
			return appSubRepo.findAll(pageable);
		else if(domain != null)
			return appSubRepo.findAllByDomain(domain, pageable);
		else if(application != null)
			return appSubRepo.findAllByApplication(application, pageable);
		else {
			Optional<ApplicationSubscription> res = appSubRepo.findByDomainAndApplication(domain, application);
			return new PageImpl<ApplicationSubscription>(Arrays.asList(res.orElse(null)), 
														pageable, 
														res.isPresent() ? 1 : 0);
		}
	}

	@Override
	public ApplicationSubscription subscribe(ApplicationSubscription appSub) throws ObjectAlreadyExistsException, ProcessingException {
		checkParam(appSub);
		
		if(appSubRepo.existsById(appSub.getId()))
			appSub = appSubRepo.findById(appSub.getId()).get();
		
		if(appSub.isDeleted())
			appSub.setDeleted(false);
			
		appSub.setActive(true);
			
		try {
			return appSubRepo.save(appSub);
		} catch(Exception ex) {
			throw new ProcessingException("Error during subscription.", ex);
		}
	}

	@Override
	public ApplicationSubscription subscribe(Long applicationId, Long domainId, boolean active) throws ObjectAlreadyExistsException, ObjectNotFoundException, ProcessingException {
		Domain domain = getDomain(domainId);
		Application application = getApplication(applicationId);

		ApplicationSubscription appSub = appSubRepo.findByDomainAndApplicationId(domainId, applicationId).orElse(new ApplicationSubscription(domain, application));

		return subscribe(appSub);
	}

	@Override
	public ApplicationSubscription subscribe(Application application, Domain domain, boolean active) throws ObjectAlreadyExistsException, ProcessingException {
		checkParam(application, domain);
		
		ApplicationSubscription appSub = appSubRepo.findByDomainAndApplication(domain, application).orElse(new ApplicationSubscription(domain, application));

		return subscribe(appSub);
	}

	@Override
	public boolean unsubscribe(ApplicationSubscription appSub) throws ProcessingException, ObjectNotFoundException {
		checkParam(appSub);

		if(appSubRepo.isDeleted(appSub.getDomain(), appSub.getApplication()))
			return true;
		else if(!appSubRepo.existsById(appSub.getId()))
			throw new ObjectNotFoundException("Application subscription not found.");
		
		appSub.setActive(false);
		appSub.setDeleted(true);

		try {
			appSubRepo.save(appSub);
			return true;
		}catch(Exception ex) {
			throw new ProcessingException("Unable to unsubscribe application", ex);
		}
	}

	@Override
	public boolean unsubscribe(Long applicationId, Long domainId) throws ProcessingException, ObjectNotFoundException {
		checkParam(applicationId, domainId);

		ApplicationSubscription appSub = findApplicationSubscription(applicationId, domainId);

		return unsubscribe(appSub);
	}

	@Override
	public boolean unsubscribe(Application application, Domain domain) throws ProcessingException, ObjectNotFoundException {		
		checkParam(application, domain);
		
		ApplicationSubscription appSub = findApplicationSubscription(application, domain);		
		return unsubscribe(appSub);
	}


	@Override
	public List<Application> getSubscribedApplications() {		
		return getSubscribedApplications(null);
	}

	@Override
	public List<Application> getSubscribedApplications(Long domainId) {		
		return (domainId != null ? appSubRepo.findApplicationBriefAllByDomain(domainId) : appSubRepo.findApplicationBriefAllBy());
	}

	protected ApplicationSubscription findApplicationSubscription(Id id) throws ObjectNotFoundException {
		return appSubRepo.findById(id).orElseThrow(() ->
                new ObjectNotFoundException("Application subscription not found."));
	}
	
	private ApplicationSubscription findApplicationSubscription(Long applicationId, Long domainId)
			throws ObjectNotFoundException {
		return appSubRepo.findByDomainAndApplicationId(domainId, applicationId).orElseThrow(() ->
                new ObjectNotFoundException("Application subscription not found."));
	}
	
	private ApplicationSubscription findApplicationSubscription(Application application, Domain domain)
			throws ObjectNotFoundException {		
		return appSubRepo.findByDomainAndApplication(domain, application).orElseThrow(() ->
                new ObjectNotFoundException("Application subscription not found."));
	}
	
	protected Domain getDomain(Long domainId) throws ObjectNotFoundException {
		checkParam(domainId, "domainId");
		return domains.findDomain(domainId).orElseThrow(() ->
                new ObjectNotFoundException("Domain " + domainId + " not found."));
	}

	protected Application getApplication(Long applicationId) throws ObjectNotFoundException {
		checkParam(applicationId, "applicationId");
		return applications.findApplication(applicationId).orElseThrow(() ->
                new ObjectNotFoundException("Application " + applicationId + " not found."));
	}

	
	
	protected void checkParam(ApplicationSubscription appSub) {
		if(appSub == null)
			throw new IllegalArgumentException("appSub is null");
		checkParam(appSub.getId());
	}
	
	protected void checkParam(Long id, String name) {
		if(id == null)
			throw new IllegalArgumentException((name != null ? name : "id ") + " is null");
	}
	
	protected void checkParam(Id id) {
		if(id == null)
			throw new IllegalArgumentException("id is null");
		checkParam(id.getApplication(), id.getDomain());
	}
	
	protected void checkParam(Long applicationId, Long domainId) {
		if(applicationId == null)
			throw new IllegalArgumentException("applicationId is null");
		if(domainId == null)
			throw new IllegalArgumentException("domainId is null");
	}
	
	protected void checkParam(Application application, Domain domain) {
		if(application == null)
			throw new IllegalArgumentException("application is null");
		if(domain == null)
			throw new IllegalArgumentException("domain is null");
		checkParam(application.getId(), domain.getId());
	}
	
}
