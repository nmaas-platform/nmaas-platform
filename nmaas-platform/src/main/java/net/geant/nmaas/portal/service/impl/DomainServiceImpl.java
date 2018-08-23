package net.geant.nmaas.portal.service.impl;

import net.geant.nmaas.dcn.deployment.DcnRepositoryManager;
import net.geant.nmaas.dcn.deployment.entities.DcnInfo;
import net.geant.nmaas.dcn.deployment.entities.DcnSpec;
import net.geant.nmaas.orchestration.exceptions.InvalidDomainException;
import net.geant.nmaas.portal.exceptions.ObjectNotFoundException;
import net.geant.nmaas.portal.exceptions.ProcessingException;
import net.geant.nmaas.portal.persistent.entity.Domain;
import net.geant.nmaas.portal.persistent.entity.Role;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.persistent.entity.UserRole;
import net.geant.nmaas.portal.persistent.repositories.DomainRepository;
import net.geant.nmaas.portal.persistent.repositories.UserRoleRepository;
import net.geant.nmaas.portal.service.DomainService;
import net.geant.nmaas.portal.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DomainServiceImpl implements DomainService {

	public interface CodenameValidator {
		boolean valid(String codename);
	}

	CodenameValidator validator;
	
	@Value("${domain.global:GLOBAL}")
	String GLOBAL_DOMAIN;

	DomainRepository domainRepo;

	UserService users;

	UserRoleRepository userRoleRepo;

	DcnRepositoryManager dcnRepositoryManager;

	@Autowired
	public DomainServiceImpl(CodenameValidator validator, DomainRepository domainRepo, UserService users, UserRoleRepository userRoleRepo, DcnRepositoryManager dcnRepositoryManager){
		this.validator = validator;
		this.domainRepo = domainRepo;
		this.users = users;
		this.userRoleRepo = userRoleRepo;
		this.dcnRepositoryManager = dcnRepositoryManager;
	}

	@Override
	public List<Domain> getDomains() {		
		return domainRepo.findAll();
	}
	
	@Override
	public Page<Domain> getDomains(Pageable pageable) {		
		return domainRepo.findAll(pageable);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Domain createGlobalDomain() throws ProcessingException {
		Optional<Domain> globalDomain = getGlobalDomain();
		if(globalDomain.isPresent())
			return globalDomain.get();
		else
			return createDomain(GLOBAL_DOMAIN, GLOBAL_DOMAIN);
	}
	
	@Override
	public Optional<Domain> getGlobalDomain() {		
		return domainRepo.findByName(GLOBAL_DOMAIN);
	}
	
	
	@Override
	public boolean existsDomain(String name) {
		checkParam(name);
		return domainRepo.existsByName(name);
	}
	
	@Override
	public boolean existsDomainByCodename(String codename) {
		checkParam(codename);
		return domainRepo.existsByCodename(codename);
	}

	@Override
	public Domain createDomain(String name, String codename) throws ProcessingException {
		return createDomain(name, codename, true);
	}

	@Override
	public Domain createDomain(String name, String codename, boolean active) throws ProcessingException{
		return createDomain(name, codename, active, false, null, null);
	}
	
	@Override
	public Domain createDomain(String name, String codename, boolean active, boolean dcnConfigured, String kubernetesNamespace, String kubernetesStorageClass) throws ProcessingException {
		checkParam(name);
		checkParam(codename);

		Optional.ofNullable(validator)
				.map(v -> v.valid(codename))
				.filter(result -> result)
				.orElseThrow(() -> new ProcessingException("Domain codename is not valid")); 
		
		try {
			return domainRepo.save(new Domain(name, codename, active, dcnConfigured, kubernetesNamespace, kubernetesStorageClass));
		} catch(Exception ex) {
			throw new ProcessingException("Unable to create new domain with given name or codename.");
		}
	}

	@Override
	public void storeDcnInfo(String domain) throws InvalidDomainException {
		this.dcnRepositoryManager.storeDcnInfo(new DcnInfo(constructDcnSpec(domain)));
	}

	private DcnSpec constructDcnSpec(String domain) {
		return new DcnSpec(buildDcnName(domain), domain);
	}

	private String buildDcnName(String domain) {
		return domain + "-" + System.nanoTime();
	}
	
	@Override
	public Optional<Domain> findDomain(String name) {
		return domainRepo.findByName(name);
	}
	
	@Override
	public Optional<Domain> findDomain(Long id) {		
		return domainRepo.findById(id);
	}
	
	@Override
	public Optional<Domain> findDomainByCodename(String codename) {		
		return domainRepo.findByCodename(codename);
	}

	@Override
	public void updateDomain(Domain domain) throws ProcessingException {		
		checkParam(domain);
		checkGlobal(domain);
		if(domain.getId() == null)
			throw new ProcessingException("Cannot update domain. Domain not created previously?");
		domainRepo.save(domain);
	}

	@Override
	public boolean removeDomain(Long id) {
		return findDomain(id).map(toRemove -> {checkGlobal(toRemove);domainRepo.delete(toRemove); return true;}).orElse(false);
	}

	@Override
	public List<User> getMembers(Long id) {
		return userRoleRepo.findDomainMembers(id);		
	}

	public void addMemberRole(Long domainId, Long userId, Role role) throws ObjectNotFoundException {
		checkParams(domainId, userId);
		checkParams(role);
			
		Domain domain = getDomain(domainId);
		
		User user = getUser(userId);

		if(userRoleRepo.findByDomainAndUserAndRole(domain, user, role) == null) {
			removePreviousRoleInDomain(domain, user);
			userRoleRepo.save(new UserRole(user, domain, role));
		}
	}

	private void removePreviousRoleInDomain(Domain domain, User user){
		Optional<UserRole> previousRole = user.getRoles().stream().filter(value -> value.getDomain().getId().equals(domain.getId())).findAny();
		previousRole.ifPresent(value -> userRoleRepo.deleteBy(user, domain, value.getRole()));
	}

	private User getUser(Long userId) throws ObjectNotFoundException {
		return users.findById(userId).orElseThrow(() -> new ObjectNotFoundException("User not found"));
	}

	private Domain getDomain(Long domainId) throws ObjectNotFoundException {
		return findDomain(domainId).orElseThrow(() -> new ObjectNotFoundException("Domain not found"));
	}

	@Override
	public void removeMemberRole(Long domainId, Long userId, Role role) throws ObjectNotFoundException {
		checkParams(domainId, userId);
		checkParams(role);
			
		Domain domain = getDomain(domainId);
		
		User user = getUser(userId);
		
		userRoleRepo.deleteBy(user, domain, role);
	}

	@Override
	public void removeMember(Long domainId, Long userId) throws ObjectNotFoundException {
		checkParams(domainId, userId);
		 
		Domain domain = getDomain(domainId);
		
		User user = getUser(userId);
		
		userRoleRepo.deleteBy(user, domain);		
	}

	@Override
	public Set<Role> getMemberRoles(Long domainId, Long userId) throws ObjectNotFoundException {
		checkParams(domainId, userId);
		
		Domain domain = getDomain(domainId);
		
		User user = getUser(userId);;
		
		return userRoleRepo.findRolesByDomainAndUser(domain, user);
	}

	@Override
	public User getMember(Long domainId, Long userId) throws ProcessingException {
		checkParams(domainId, userId);
		
		Domain domain = getDomain(domainId);
		
		User user = getUser(userId);;
				
		User userMember = userRoleRepo.findDomainMember(domain, user);
		if(userMember == null)
			throw new ProcessingException("User is not domain member");
		
		return userMember;
	}

	@Override
	public Set<Domain> getUserDomains(Long userId) throws ObjectNotFoundException {
		checkParams(userId);

		User user = getUser(userId);
				
		return user.getRoles().stream().map(ur -> ur.getDomain()).collect(Collectors.toSet());
	}

	protected void checkParam(String name) {
		if(name == null)
			throw new IllegalArgumentException("name is null");
	}
	
	protected void checkParam(Domain domain) {
		if(domain == null)
			throw new IllegalArgumentException("domain is null");
	}
	
	protected void checkParams(Long id) {
		if(id == null)
			throw new IllegalArgumentException("id is null");
	}
	
	protected void checkParams(Role role) {
		if(role == null)
			throw new IllegalArgumentException("role is null");
	}
	
	protected void checkParams(Long domainId, Long userId) {
		if(domainId == null)
			throw new IllegalArgumentException("domainId is null");
		if(userId == null)
			throw new IllegalArgumentException("userId is null");		
	}

	protected void checkGlobal(Domain domain){
		if(domain.getCodename().equals(GLOBAL_DOMAIN))
			throw new IllegalArgumentException("Global domain can't be updated or removed");
	}
	
}
