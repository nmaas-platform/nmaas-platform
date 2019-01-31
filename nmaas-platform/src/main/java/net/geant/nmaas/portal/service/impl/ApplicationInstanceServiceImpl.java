package net.geant.nmaas.portal.service.impl;

import java.util.Map;
import java.util.stream.Collectors;
import net.geant.nmaas.portal.exceptions.ApplicationSubscriptionNotActiveException;
import net.geant.nmaas.portal.exceptions.ObjectNotFoundException;
import net.geant.nmaas.portal.persistent.entity.AppInstance;
import net.geant.nmaas.portal.persistent.entity.Application;
import net.geant.nmaas.portal.persistent.entity.Domain;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.persistent.repositories.AppInstanceRepository;
import net.geant.nmaas.portal.service.ApplicationInstanceService;
import net.geant.nmaas.portal.service.ApplicationService;
import net.geant.nmaas.portal.service.ApplicationSubscriptionService;
import net.geant.nmaas.portal.service.DomainService;
import net.geant.nmaas.portal.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;

@Service
public class ApplicationInstanceServiceImpl implements ApplicationInstanceService {

	private final AppInstanceRepository appInstanceRepo;
	
	private final ApplicationService applications;
	
	private final DomainService domains;
	
	private final UserService users;
	
	private final ApplicationSubscriptionService applicationSubscriptions;

	private final DomainServiceImpl.CodenameValidator validator;

	@Autowired
	public ApplicationInstanceServiceImpl(AppInstanceRepository appInstanceRepo, ApplicationService applications, DomainService domains, UserService users, ApplicationSubscriptionService applicationSubscriptions, @Qualifier("InstanceNameValidator") DomainServiceImpl.CodenameValidator validator) {
		this.appInstanceRepo = appInstanceRepo;
		this.applications = applications;
		this.domains = domains;
		this.users = users;
		this.applicationSubscriptions = applicationSubscriptions;
		this.validator = validator;
	}

	@Override
	public AppInstance create(Long domainId, Long applicationId, String name) {
		Application app = applications.findApplication(applicationId).orElseThrow(() -> new ObjectNotFoundException("Application not found."));
		Domain domain = domains.findDomain(domainId).orElseThrow(() -> new ObjectNotFoundException("Domain not found."));
		return create(domain, app, name);
	}

	@Override
	public AppInstance create(Domain domain, Application application, String name) {
		checkParam(domain);
		checkParam(application);
		checkNameCharacters(name);
		checkNameUniqueness(domain, name);
		if(applicationSubscriptions.isActive(application, domain))
			return appInstanceRepo.save(new AppInstance(application, domain, name));
		else
			throw new ApplicationSubscriptionNotActiveException("Application subscription is missing or not active.");
	}

	@Override
	public void delete(Long appInstanceId) {
		checkParam(appInstanceId);
		find(appInstanceId).ifPresent(appInstanceRepo::delete);
	}
	
	@Override
	public void update(AppInstance appInstance) {
		checkParam(appInstance);
		appInstanceRepo.save(appInstance);
	}

	@Override
	public Optional<AppInstance> find(Long appInstanceId) {
		checkParam(appInstanceId);
		return appInstanceRepo.findById(appInstanceId);
	}

	@Override
	public List<AppInstance> findAll() {
		return appInstanceRepo.findAll();
	}

	@Override
	public Page<AppInstance> findAll(Pageable pageable) {
		return appInstanceRepo.findAll(pageable);
	}

	@Override
	public List<AppInstance> findAllByOwner(Long userId) {
		checkParam(userId);
		User user = users.findById(userId).orElseThrow(() -> new ObjectNotFoundException("user not found"));
		return findAllByOwner(user);
	}

	@Override
	public List<AppInstance> findAllByOwner(User owner) {
		checkParam(owner);
		return appInstanceRepo.findAllByOwner(owner);
	}

	@Override
	public List<AppInstance> findAllByOwner(Long userId, Long domainId) {
		User owner = getUser(userId);
		Domain domain = getDomain(domainId);
		return findAllByOwner(owner, domain);
	}

	@Override
	public List<AppInstance> findAllByOwner(User owner, Domain domain) {
		checkParam(owner);
		checkParam(domain);
		return appInstanceRepo.findAllByOwnerAndDomain(owner, domain);
	}

	@Override
	public Map<Long, String> getAllInstanceNamesByApplicationNameOwnerAndDomain(String appName, User owner, Domain domain){
		return this.findAllByOwner(owner, domain).stream()
				.filter(app -> app.getApplication().getName().equalsIgnoreCase(appName))
				.collect(Collectors.toMap(AppInstance::getId, AppInstance::getName));
	}

	@Override
	public Page<AppInstance> findAllByOwner(Long userId, Pageable pageable) {
		User user = getUser(userId);
		return findAllByOwner(user, pageable);
	}

	@Override
	public Page<AppInstance> findAllByOwner(User owner, Pageable pageable) {
		checkParam(owner);
		return appInstanceRepo.findAllByOwner(owner, pageable);
	}

	@Override
	public Page<AppInstance> findAllByOwner(Long userId, Long domainId, Pageable pageable) {
		User owner = getUser(userId);
		Domain domain = getDomain(domainId);
		return findAllByOwner(owner, domain, pageable);
	}

	@Override
	public Page<AppInstance> findAllByOwner(User owner, Domain domain, Pageable pageable) {
		checkParam(owner);
		checkParam(domain);
		return appInstanceRepo.findAllByOwnerAndDomain(owner, domain, pageable);
	}

	@Override
	public List<AppInstance> findAllByDomain(Long domainId) {
		Domain domain = getDomain(domainId);
		return findAllByDomain(domain);
	}

	@Override
	public List<AppInstance> findAllByDomain(Domain domain) {
		checkParam(domain);
		return appInstanceRepo.findAllByDomain(domain);
	}

	@Override
	public Page<AppInstance> findAllByDomain(Long domainId, Pageable pageable) {
		Domain domain = getDomain(domainId);
		return findAllByDomain(domain, pageable);
	}

	@Override
	public Page<AppInstance> findAllByDomain(Domain domain, Pageable pageable) {
		checkParam(domain);
		return appInstanceRepo.findAllByDomain(domain, pageable);
	}
	
	private void checkParam(AppInstance appInstance) {
		if(appInstance == null)
			throw new IllegalArgumentException("appInstance is null");
		checkParam(appInstance.getId());
	}
	
	private void checkParam(Long id) {
		checkArgument(id != null, "Id is null");
	}
	
	private void checkParam(Application application) {
		if(application == null)
			throw new IllegalArgumentException("application is null");
		checkParam(application.getId());
	}
	
	private void checkParam(Domain domain) {
		if(domain == null)
			throw new IllegalArgumentException("domain is null");
		checkParam(domain.getId());		
	}
	
	private void checkParam(User user) {
		if(user == null)
			throw new IllegalArgumentException("user is null");
		checkParam(user.getId())    ;
	}

	private void checkNameUniqueness(Domain domain, String name){
		if(findAllByDomain(domain).stream().anyMatch(s -> s.getName().equalsIgnoreCase(name))){
			throw new IllegalArgumentException("Name is already taken");
		}
	}

	private void checkNameCharacters(String name){
	    checkArgument(validator.valid(name), "Instance name is not valid");
	}

	protected Domain getDomain(Long domainId) {
		checkParam(domainId);
		return domains.findDomain(domainId).orElseThrow(() -> new ObjectNotFoundException("Domain not found"));
	}
	
	protected User getUser(Long userId) {
		checkParam(userId);
		return users.findById(userId).orElseThrow(() -> new ObjectNotFoundException("User not found"));
	}
	
}
