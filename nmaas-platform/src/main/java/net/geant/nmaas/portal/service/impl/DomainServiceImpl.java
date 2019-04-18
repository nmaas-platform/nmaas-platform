package net.geant.nmaas.portal.service.impl;

import net.geant.nmaas.dcn.deployment.DcnDeploymentType;
import net.geant.nmaas.dcn.deployment.DcnRepositoryManager;
import net.geant.nmaas.dcn.deployment.entities.DcnInfo;
import net.geant.nmaas.dcn.deployment.entities.DcnSpec;
import net.geant.nmaas.orchestration.repositories.DomainTechDetailsRepository;
import net.geant.nmaas.portal.api.domain.DomainRequest;
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
import org.apache.commons.lang.StringUtils;
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

	private CodenameValidator validator;

	private CodenameValidator namespaceValidator;
	
	@Value("${domain.global:GLOBAL}")
	String globalDomain;

	private DomainRepository domainRepo;

	private DomainTechDetailsRepository domainTechDetailsRepo;

	private UserService users;

	private UserRoleRepository userRoleRepo;

	private DcnRepositoryManager dcnRepositoryManager;

	private ModelMapper modelMapper;

	@Autowired
	public DomainServiceImpl(CodenameValidator validator,
							 @Qualifier("NamespaceValidator") CodenameValidator namespaceValidator,
							 DomainRepository domainRepo,
							 DomainTechDetailsRepository domainTechDetailsRepo,
							 UserService users,
							 UserRoleRepository userRoleRepo,
							 DcnRepositoryManager dcnRepositoryManager,
							 ModelMapper modelMapper
	){
		this.validator = validator;
		this.namespaceValidator = namespaceValidator;
		this.domainRepo = domainRepo;
		this.domainTechDetailsRepo = domainTechDetailsRepo;
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
		return getGlobalDomain().orElseGet(() -> createDomain(new DomainRequest(this.globalDomain, this.globalDomain.toLowerCase(), true)));
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
		return domainTechDetailsRepo.existsByExternalServiceDomain(externalServiceDomain);
	}

	
	@Override
	public Domain createDomain(DomainRequest request) {
		checkParam(request.getName());
		checkParam(request.getCodename());

		if(!Optional.ofNullable(validator).map(v -> v.valid(request.getCodename())).filter(result -> result).isPresent()){
			throw new ProcessingException("Domain codename is not valid");
		}
		if(StringUtils.isEmpty(request.getDomainTechDetails().getKubernetesNamespace())){
			request.getDomainTechDetails().setKubernetesNamespace(request.getCodename());
		}
		if(!namespaceValidator.valid(request.getDomainTechDetails().getKubernetesNamespace())){
			throw new ProcessingException("Kubernetes namespace is not valid");
		}
		if(StringUtils.isNotEmpty(request.getDomainTechDetails().getExternalServiceDomain())){
			checkArgument(!domainTechDetailsRepo.existsByExternalServiceDomain(request.getDomainTechDetails().getExternalServiceDomain()), "External service domain is not unique");
		}
		this.setCodenames(request);
		try {
			return domainRepo.save(modelMapper.map(request, Domain.class));
		} catch(Exception ex) {
			throw new ProcessingException("Unable to create new domain with given name or codename.");
		}
	}

	private void setCodenames(DomainRequest request){
		request.getDomainTechDetails().setDomainCodename(request.getCodename());
		request.getDomainDcnDetails().setDomainCodename(request.getCodename());
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
		if(StringUtils.isEmpty(domain.getDomainTechDetails().getKubernetesNamespace())){
			domain.getDomainTechDetails().setKubernetesNamespace(domain.getCodename());
		}
		if(!namespaceValidator.valid(domain.getDomainTechDetails().getKubernetesNamespace())){
			throw new ProcessingException("Kubernetes namespace is not valid.");
		}
		domainRepo.save(domain);
	}

	@Override
	public Domain changeDcnConfiguredFlag(Long domainId, boolean dcnConfigured){
		checkParams(domainId);
		Domain domain = findDomain(domainId).orElseThrow(() -> new MissingElementException("Domain not found"));
		checkGlobal(domain);
		domain.getDomainDcnDetails().setDcnConfigured(dcnConfigured);
		return domainRepo.save(domain);
	}

	@Override
	public void changeDomainState(Long domainId, boolean active){
		checkParams(domainId);
		Domain domain = findDomain(domainId).orElseThrow(() -> new MissingElementException("Domain not found"));
		checkGlobal(domain);
		domain.setActive(active);
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
		user.getRoles().stream()
				.filter(value -> value.getDomain().getId().equals(domain.getId()))
				.findAny()
				.ifPresent(value -> userRoleRepo.deleteBy(user.getId(), domain.getId(), value.getRole()));
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
		userRoleRepo.deleteBy(userId, domainId, role);
	}

	@Override
	public void removeMember(Long domainId, Long userId) {
		checkParams(domainId, userId);
		userRoleRepo.deleteBy(userId, domainId);
	}

	@Override
	public Set<Role> getMemberRoles(Long domainId, Long userId) {
		checkParams(domainId, userId);
		return userRoleRepo.findRolesByDomainAndUser(domainId, userId);
	}

	@Override
	public User getMember(Long domainId, Long userId) {
		checkParams(domainId, userId);
		return userRoleRepo.findDomainMember(domainId, userId)
				.orElseThrow(() -> new ProcessingException("User is not domain member"));
	}

	@Override
	public Set<Domain> getUserDomains(Long userId) {
		checkParams(userId);
		return getUser(userId).getRoles().stream()
				.map(UserRole::getDomain)
				.collect(Collectors.toSet());
	}

	@Override
	public List<net.geant.nmaas.portal.api.domain.User> findUsersWithDomainAdminRole(String domain){
		return this.userRoleRepo.findDomainMembers(domain).stream()
				.filter(user -> user.getRoles().stream().anyMatch(role -> role.getRole().name().equalsIgnoreCase(Role.ROLE_DOMAIN_ADMIN.name()) && role.getDomain().getCodename().equals(domain)))
				.map(user -> modelMapper.map(user, net.geant.nmaas.portal.api.domain.User.class))
				.collect(Collectors.toList());
	}

	protected void checkParam(String name) {
		if(StringUtils.isEmpty(name)) {
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
