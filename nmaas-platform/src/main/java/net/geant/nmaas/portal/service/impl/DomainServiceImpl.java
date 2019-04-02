package net.geant.nmaas.portal.service.impl;

import net.geant.nmaas.dcn.deployment.DcnDeploymentType;
import net.geant.nmaas.dcn.deployment.DcnRepositoryManager;
import net.geant.nmaas.dcn.deployment.entities.DcnInfo;
import net.geant.nmaas.dcn.deployment.entities.DcnSpec;
import net.geant.nmaas.portal.api.exception.MissingElementException;
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
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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

import static com.google.common.base.Preconditions.checkArgument;
import static net.geant.nmaas.portal.persistent.entity.Role.ROLE_GUEST;

@Service
public class DomainServiceImpl implements DomainService {

	public interface CodenameValidator {
		boolean valid(String codename);
	}

	CodenameValidator validator;

	CodenameValidator namespaceValidator;
	
	@Value("${domain.global:GLOBAL}")
	String globalDomain;

	DomainRepository domainRepo;

	UserService users;

	UserRoleRepository userRoleRepo;

	DcnRepositoryManager dcnRepositoryManager;

	ModelMapper modelMapper;

	@Autowired
	public DomainServiceImpl(CodenameValidator validator,
							 @Qualifier("NamespaceValidator") CodenameValidator namespaceValidator,
							 DomainRepository domainRepo,
							 UserService users,
							 UserRoleRepository userRoleRepo,
							 DcnRepositoryManager dcnRepositoryManager,
							 ModelMapper modelMapper
	){
		this.validator = validator;
		this.namespaceValidator = namespaceValidator;
		this.domainRepo = domainRepo;
		this.users = users;
		this.userRoleRepo = userRoleRepo;
		this.dcnRepositoryManager = dcnRepositoryManager;
		this.modelMapper = modelMapper;
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
	public Domain createGlobalDomain() {
		Optional<Domain> globalDomainOptional = getGlobalDomain();
		return globalDomainOptional.orElseGet(() -> createDomain(this.globalDomain, this.globalDomain.toLowerCase()));
	}
	
	@Override
	public Optional<Domain> getGlobalDomain() {		
		return domainRepo.findByName(globalDomain);
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
	public boolean existsDomainByExternalServiceDomain(String externalServiceDomain) {
		return domainRepo.existsByExternalServiceDomain(externalServiceDomain);
	}

	@Override
	public Domain createDomain(String name, String codename) {
		return createDomain(name, codename, true);
	}

	@Override
	public Domain createDomain(String name, String codename, boolean active) {
		return createDomain(name, codename, active, false, null, null, null, null);
	}
	
	@Override
	public Domain createDomain(String name, String codename, boolean active, boolean dcnConfigured, String kubernetesNamespace, String kubernetesStorageClass, String externalServiceDomain, DcnDeploymentType dcnDeploymentType) {
		checkParam(name);
		checkParam(codename);

		if(!Optional.ofNullable(validator).map(v -> v.valid(codename)).filter(result -> result).isPresent()){
			throw new ProcessingException("Domain codename is not valid");
		}
		if(kubernetesNamespace == null || kubernetesNamespace.isEmpty()){
			kubernetesNamespace = codename;
		}
		if(!namespaceValidator.valid(kubernetesNamespace)){
			throw new ProcessingException("Kubernetes namespace is not valid");
		}
		if(externalServiceDomain != null && !externalServiceDomain.isEmpty()){
			checkArgument(!domainRepo.existsByExternalServiceDomain(externalServiceDomain), "External service domain is not unique");
		}
		try {
			return domainRepo.save(new Domain(name, codename, active, dcnConfigured, kubernetesNamespace, kubernetesStorageClass, externalServiceDomain, dcnDeploymentType));
		} catch(Exception ex) {
			throw new ProcessingException("Unable to create new domain with given name or codename.");
		}
	}

	@Override
	public void storeDcnInfo(String domain, DcnDeploymentType dcnDeploymentType) {
		this.dcnRepositoryManager.storeDcnInfo(new DcnInfo(constructDcnSpec(domain, dcnDeploymentType)));
	}

	private DcnSpec constructDcnSpec(String domain, DcnDeploymentType dcnDeploymentType) {
		return new DcnSpec(buildDcnName(domain), domain, dcnDeploymentType);
	}

	private String buildDcnName(String domain) {
		return domain + "-" + System.nanoTime();
	}

	@Override
    public void updateDcnInfo(String domain, DcnDeploymentType dcnDeploymentType){
	    this.dcnRepositoryManager.updateDcnDeploymentType(domain, dcnDeploymentType);
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
	public void updateDomain(Domain domain) {		
		checkParam(domain);
		checkGlobal(domain);
		if(domain.getId() == null)
			throw new ProcessingException("Cannot update domain. Domain not created previously?");
		if(domain.getKubernetesNamespace() == null || domain.getKubernetesNamespace().isEmpty()){
			domain.getDomainTechDetails().setKubernetesNamespace(domain.getCodename());
		}
		if(!namespaceValidator.valid(domain.getKubernetesNamespace())){
			throw new ProcessingException("Namespace is not valid.");
		}
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

	public void addMemberRole(Long domainId, Long userId, Role role) {
		checkParams(domainId, userId);
		checkParams(role);
			
		Domain domain = getDomain(domainId);
		
		User user = getUser(userId);

		if(userRoleRepo.findByDomainAndUserAndRole(domain, user, role) == null) {
			removePreviousRoleInDomain(domain, user);
			userRoleRepo.save(new UserRole(user, domain, role));
		}
	}

	@Override
	public void addGlobalGuestUserRoleIfMissing(Long userId) {
		Optional<Domain> globalDomainOptional = this.getGlobalDomain();
		if(globalDomainOptional.isPresent()){
			Long globalId = globalDomainOptional.get().getId();
			try{
				if(this.getMemberRoles(globalId, userId).isEmpty()){
					this.addMemberRole(globalId, userId, ROLE_GUEST);
				}
			} catch(ObjectNotFoundException e){
				throw new MissingElementException(e.getMessage());
			}
		}
	}

	private void removePreviousRoleInDomain(Domain domain, User user){
		Optional<UserRole> previousRole = user.getRoles().stream().filter(value -> value.getDomain().getId().equals(domain.getId())).findAny();
		previousRole.ifPresent(value -> userRoleRepo.deleteBy(user, domain, value.getRole()));
	}

	private User getUser(Long userId) {
		return users.findById(userId).orElseThrow(() -> new ObjectNotFoundException("User not found"));
	}

	private Domain getDomain(Long domainId) {
		return findDomain(domainId).orElseThrow(() -> new ObjectNotFoundException("Domain not found"));
	}

	@Override
	public void removeMemberRole(Long domainId, Long userId, Role role) {
		checkParams(domainId, userId);
		checkParams(role);
			
		Domain domain = getDomain(domainId);
		User user = getUser(userId);
		
		userRoleRepo.deleteBy(user, domain, role);
	}

	@Override
	public void removeMember(Long domainId, Long userId) {
		checkParams(domainId, userId);
		 
		Domain domain = getDomain(domainId);
		User user = getUser(userId);
		
		userRoleRepo.deleteBy(user, domain);		
	}

	@Override
	public Set<Role> getMemberRoles(Long domainId, Long userId) {
		checkParams(domainId, userId);
		
		Domain domain = getDomain(domainId);
		User user = getUser(userId);
		
		return userRoleRepo.findRolesByDomainAndUser(domain, user);
	}

	@Override
	public User getMember(Long domainId, Long userId) {
		checkParams(domainId, userId);

		Domain domain = getDomain(domainId);
		User user = getUser(userId);
		User userMember = userRoleRepo.findDomainMember(domain, user);
		if(userMember == null) {
			throw new ProcessingException("User is not domain member");
		}

		return userMember;
	}

	@Override
	public Set<Domain> getUserDomains(Long userId) {
		checkParams(userId);
		User user = getUser(userId);
		return user.getRoles().stream().map(UserRole::getDomain).collect(Collectors.toSet());
	}

	@Override
	public List<net.geant.nmaas.portal.api.domain.User> findUsersWithDomainAdminRole(String domain){
		return this.userRoleRepo.findDomainMembers(domain).stream()
				.filter(user -> user.getRoles().stream().anyMatch(role -> role.getRole().name().equalsIgnoreCase(Role.ROLE_DOMAIN_ADMIN.name()) && role.getDomain().getCodename().equals(domain)))
				.map(user -> modelMapper.map(user, net.geant.nmaas.portal.api.domain.User.class))
				.collect(Collectors.toList());
	}

	protected void checkParam(String name) {
		if(name == null) {
			throw new IllegalArgumentException("Name is null");
		}
	}
	
	protected void checkParam(Domain domain) {
		if(domain == null) {
			throw new IllegalArgumentException("Domain is null");
		}
	}
	
	private void checkParams(Long id) {
		if(id == null) {
			throw new IllegalArgumentException("id is null");
		}
	}
	
	private void checkParams(Role role) {
		if(role == null) {
			throw new IllegalArgumentException("role is null");
		}
	}
	
	private void checkParams(Long domainId, Long userId) {
		if(domainId == null) {
			throw new IllegalArgumentException("domainId is null");
		}
		if(userId == null) {
			throw new IllegalArgumentException("userId is null");
		}
	}

	private void checkGlobal(Domain domain){
		if(domain.getCodename().equals(globalDomain)) {
			throw new IllegalArgumentException("Global domain can't be updated or removed");
		}
	}
	
}
