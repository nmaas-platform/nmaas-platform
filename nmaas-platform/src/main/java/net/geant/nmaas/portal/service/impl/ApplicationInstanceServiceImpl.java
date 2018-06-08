package net.geant.nmaas.portal.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

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

@Service
public class ApplicationInstanceServiceImpl implements ApplicationInstanceService {

	@Autowired
	AppInstanceRepository appInstanceRepo;
	
	@Autowired
	ApplicationService applications;
	
	@Autowired
	DomainService domains;
	
	@Autowired
	UserService users;
	
	@Autowired
	ApplicationSubscriptionService applicationSubscriptions;

	@Override
	public AppInstance create(Long domainId, Long applicationId, String name) throws ObjectNotFoundException, ApplicationSubscriptionNotActiveException {
		Application app = applications.findApplication(applicationId).orElseThrow(() -> new ObjectNotFoundException("Application not found."));
		Domain domain = domains.findDomain(domainId).orElseThrow(() -> new ObjectNotFoundException("Domain not found."));
		return create(domain, app, name);
	}

	@Override
	public AppInstance create(Domain domain, Application application, String name) throws ApplicationSubscriptionNotActiveException {		
		checkParam(domain);
		checkParam(application);
		checkNameUniqueness(domain, name);
		checkNameCharacters(name);
		if(applicationSubscriptions.isActive(application, domain))
			return appInstanceRepo.save(new AppInstance(application, domain, name));
		else
			throw new ApplicationSubscriptionNotActiveException("Application subscription is missing or not active.");
	}

	@Override
	public void delete(Long appInstanceId) {
		checkParam(appInstanceId);
		
		find(appInstanceId).ifPresent((appInstance) -> appInstanceRepo.delete(appInstance));
	}
	
	@Override
	public void update(AppInstance appInstance) {
		checkParam(appInstance);
		appInstanceRepo.save(appInstance);
	}



	@Override
	public Optional<AppInstance> find(Long appInstanceId) {
		checkParam(appInstanceId);
		return Optional.ofNullable(appInstanceRepo.findOne(appInstanceId));
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
	public List<AppInstance> findAllByOwner(Long userId) throws ObjectNotFoundException {
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
	public List<AppInstance> findAllByOwner(Long userId, Long domainId) throws ObjectNotFoundException {
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
	public Page<AppInstance> findAllByOwner(Long userId, Pageable pageable) throws ObjectNotFoundException {
		User user = getUser(userId);
		return findAllByOwner(user, pageable);
	}

	@Override
	public Page<AppInstance> findAllByOwner(User owner, Pageable pageable) {
		checkParam(owner);
		return appInstanceRepo.findAllByOwner(owner, pageable);
	}

	@Override
	public Page<AppInstance> findAllByOwner(Long userId, Long domainId, Pageable pageable) throws ObjectNotFoundException {
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
	public List<AppInstance> findAllByDomain(Long domainId) throws ObjectNotFoundException {
		Domain domain = getDomain(domainId);
		return findAllByDomain(domain);
	}

	@Override
	public List<AppInstance> findAllByDomain(Domain domain) {
		checkParam(domain);
		return appInstanceRepo.findAllByDomain(domain);
	}

	@Override
	public Page<AppInstance> findAllByDomain(Long domainId, Pageable pageable) throws ObjectNotFoundException {
		Domain domain = getDomain(domainId);
		return findAllByDomain(domain, pageable);
	}

	@Override
	public Page<AppInstance> findAllByDomain(Domain domain, Pageable pageable) {
		checkParam(domain);
		return appInstanceRepo.findAllByDomain(domain, pageable);
	}
	
	protected void checkParam(AppInstance appInstance) {
		if(appInstance == null)
			throw new IllegalArgumentException("appInstance is null");
		checkParam(appInstance.getId());
	}
	
	protected void checkParam(Long id) {
		if(id == null)
			throw new IllegalArgumentException("id is null");
	}
	
	protected void checkParam(Application application) {
		if(application == null)
			throw new IllegalArgumentException("application is null");
		checkParam(application.getId());
	}
	
	protected void checkParam(Domain domain) {
		if(domain == null)
			throw new IllegalArgumentException("domain is null");
		checkParam(domain.getId());		
	}
	
	protected void checkParam(User user) {
		if(user == null)
			throw new IllegalArgumentException("user is null");
	}

	protected void checkNameUniqueness(Domain domain, String name){
		if(findAllByDomain(domain).stream().anyMatch(s -> s.getName().equalsIgnoreCase(name))){
			throw new IllegalArgumentException("Name is already taken");
		}
	}

	protected void checkNameCharacters(String name){
		String specialChars = "/[!@#$%^&*()+=\\[\\]{};':\"\\\\|,.<>\\/?]/";
		for (int x = 0;x <name.length(); x++){
			if(specialChars.contains(name.substring(x,x+1))){
				throw new IllegalArgumentException("Name contains illegal characters");
			}
		}
	}

	protected Domain getDomain(Long domainId) throws ObjectNotFoundException {
		checkParam(domainId);
		return domains.findDomain(domainId).orElseThrow(() -> new ObjectNotFoundException("Domain not found"));
	}
	
	protected User getUser(Long userId) throws ObjectNotFoundException {
		checkParam(userId);
		return users.findById(userId).orElseThrow(() -> new ObjectNotFoundException("User not found"));
	}
	
}
