package net.geant.nmaas.portal.service.impl;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import net.geant.nmaas.portal.persistent.entity.ApplicationBase;
import net.geant.nmaas.portal.service.ApplicationBaseService;
import net.geant.nmaas.portal.service.ApplicationStatePerDomainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.geant.nmaas.portal.api.exception.ProcessingException;
import net.geant.nmaas.portal.exceptions.ObjectNotFoundException;
import net.geant.nmaas.portal.persistent.entity.ApplicationSubscription;
import net.geant.nmaas.portal.persistent.entity.ApplicationSubscription.Id;
import net.geant.nmaas.portal.persistent.entity.Domain;
import net.geant.nmaas.portal.persistent.repositories.ApplicationSubscriptionRepository;
import net.geant.nmaas.portal.service.DomainService;
import net.geant.nmaas.portal.service.ApplicationSubscriptionService;

@Service
public class ApplicationSubscriptionServiceImpl implements ApplicationSubscriptionService {

	private static final String APP_NOT_FOUND_ERR_MESSAGE = "Application subscription not found.";

    private ApplicationSubscriptionRepository appSubRepo;
	
	private DomainService domains;
	
	private ApplicationBaseService applications;

	private ApplicationStatePerDomainService applicationStatePerDomainService;

	@Autowired
	public ApplicationSubscriptionServiceImpl(ApplicationSubscriptionRepository appSubRepo,
											  DomainService domains, ApplicationBaseService applications,
											  ApplicationStatePerDomainService applicationStatePerDomainService) {
		this.appSubRepo = appSubRepo;
		this.domains = domains;
		this.applications = applications;
		this.applicationStatePerDomainService = applicationStatePerDomainService;
	}
	
	
	@Override
	public boolean isActive(Id id) {
		return appSubRepo.findById(id).map(appInstance ->
				(!appInstance.isDeleted() && appInstance.isActive())).orElse(false);
	}

	@Override
	public boolean isActive(Long applicationId, Long domainId) {
		return appSubRepo.findByDomainAndApplicationId(domainId, applicationId).map(appInstance ->
				(!appInstance.isDeleted() && appInstance.isActive())).orElse(false);
	}

	@Override
	public boolean isActive(String appName, Domain domain) {
		return appSubRepo.findByDomainAndApplication(domain, applications.findByName(appName)).map(appInstance ->
				(!appInstance.isDeleted() && appInstance.isActive())).orElse(false);
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
	public boolean existsSubscription(ApplicationBase application, Domain domain) {
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
	public Optional<ApplicationSubscription> getSubscription(ApplicationBase application, Domain domain) {
		checkParam(application, domain);
		return appSubRepo.findByDomainAndApplication(domain, application);
	}
	
	@Override
	public List<ApplicationSubscription> getSubscriptions() {
		return appSubRepo.findAll();
	}

	@Override
	public List<ApplicationSubscription> getSubscriptionsBy(Long domainId, Long applicationId) {
		if(domainId != null && applicationId != null) {
			Optional<ApplicationSubscription> res = appSubRepo.findByDomainAndApplicationId(domainId, applicationId);
			return Collections.singletonList(res.orElse(null));
		}
		else if(domainId != null)
			return appSubRepo.findAllByDomain(domainId);
		else if(applicationId != null)
			return appSubRepo.findAllByApplication(applicationId);
		else
			return appSubRepo.findAll();
	}

	@Override
	public List<ApplicationSubscription> getSubscriptionsBy(Domain domain, ApplicationBase application) {
		if(domain != null && application != null) {
			Optional<ApplicationSubscription> res = appSubRepo.findByDomainAndApplication(domain, application);
			return Collections.singletonList(res.orElse(null));
		}
		else if(domain != null)
			return appSubRepo.findAllByDomain(domain);
		else if(application != null)
			return appSubRepo.findAllByApplication(application);
		else
			return appSubRepo.findAll();
	}

	@Override
	public ApplicationSubscription subscribe(ApplicationSubscription appSub) {
		checkParam(appSub);

		if(appSubRepo.existsById(appSub.getId())) {
			Optional<ApplicationSubscription> appSubOptional = appSubRepo.findById(appSub.getId());
			if(appSubOptional.isPresent()) {
				appSub = appSubOptional.get();
			}
		}
		checkParam(appSub.getApplication());
		if(appSub.isDeleted())
			appSub.setDeleted(false);
			
		appSub.setActive(true);

		if (!applicationStatePerDomainService.isApplicationEnabledInDomain(appSub.getDomain(), appSub.getApplication())) {
			throw new IllegalArgumentException("Cannot subscribe. Application is disabled in this domain");
		}
			
		try {
			return appSubRepo.save(appSub);
		} catch(Exception ex) {
			throw new ProcessingException("Error during subscription.", ex);
		}
	}

	@Override
	public ApplicationSubscription subscribe(Long applicationId, Long domainId, boolean active) {
		Domain domain = getDomain(domainId);
		ApplicationBase application = getApplication(applicationId);

		ApplicationSubscription appSub = appSubRepo.findByDomainAndApplicationId(domainId, applicationId).orElse(new ApplicationSubscription(domain, application));

		return subscribe(appSub);
	}

	@Override
	public ApplicationSubscription subscribe(ApplicationBase application, Domain domain, boolean active) {
		checkParam(application, domain);
		
		ApplicationSubscription appSub = appSubRepo.findByDomainAndApplication(domain, application).orElse(new ApplicationSubscription(domain, application));

		return subscribe(appSub);
	}

	@Override
	public void unsubscribe(ApplicationSubscription appSub) {
		checkParam(appSub);

		if(!appSubRepo.isDeleted(appSub.getDomain(), appSub.getApplication())){
			if(!appSubRepo.existsById(appSub.getId()))
				throw new ObjectNotFoundException(APP_NOT_FOUND_ERR_MESSAGE);

			appSub.setActive(false);
			appSub.setDeleted(true);

			try {
				appSubRepo.save(appSub);
			} catch(Exception ex) {
				throw new ProcessingException("Unable to unsubscribe application", ex);
			}
		}
	}

	@Override
	public void unsubscribe(Long applicationId, Long domainId) {
		checkParam(applicationId, domainId);

		ApplicationSubscription appSub = findApplicationSubscription(applicationId, domainId);

		unsubscribe(appSub);
	}

	@Override
	public void unsubscribe(ApplicationBase application, Domain domain) {
		checkParam(application, domain);
		
		ApplicationSubscription appSub = findApplicationSubscription(application, domain);		
		unsubscribe(appSub);
	}


	@Override
	public List<ApplicationBase> getSubscribedApplications() {
		return getSubscribedApplications(null);
	}

	@Override
	public List<ApplicationBase> getSubscribedApplications(Long domainId) {
		return (domainId != null ? appSubRepo.findApplicationBriefAllByDomain(domainId) : appSubRepo.findApplicationBriefAllBy());
	}
	
	private ApplicationSubscription findApplicationSubscription(Long applicationId, Long domainId) {
		return appSubRepo.findByDomainAndApplicationId(domainId, applicationId).orElseThrow(() ->
                new ObjectNotFoundException(APP_NOT_FOUND_ERR_MESSAGE));
	}


	@SuppressWarnings("unused")
	private ApplicationSubscription findApplicationSubscription(ApplicationBase application, Domain domain) {
		return appSubRepo.findByDomainAndApplication(domain, application).orElseThrow(() ->
                new ObjectNotFoundException(APP_NOT_FOUND_ERR_MESSAGE));
	}
	
	protected Domain getDomain(Long domainId) {
		checkParam(domainId, "domainId");
		return domains.findDomain(domainId).orElseThrow(() ->
                new ObjectNotFoundException("Domain " + domainId + " not found."));
	}

	protected ApplicationBase getApplication(Long applicationId) {
		checkParam(applicationId, "applicationId");
		return applications.getBaseApp(applicationId);
	}

	
	
	protected void checkParam(ApplicationSubscription appSub) {
		if(appSub == null)
			throw new IllegalArgumentException("appSub is null");
		checkParam(appSub.getId());
		if(!appSub.getDomain().isActive())
			throw new IllegalArgumentException("Domain cannot be inactive");
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
	
	protected void checkParam(ApplicationBase application, Domain domain) {
		if(application == null)
			throw new IllegalArgumentException("application is null");
		if(domain == null)
			throw new IllegalArgumentException("domain is null");
		checkParam(application.getId(), domain.getId());
	}

	protected void checkParam(ApplicationBase application){
		if(application == null)
			throw new IllegalArgumentException("application is null");
		if(!applications.isAppActive(application))
			throw new IllegalStateException("Cannot subscribe application which is not active.");
	}
	
}
