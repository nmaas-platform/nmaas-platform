package net.geant.nmaas.portal.service.impl;

import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.dcn.deployment.DcnDeploymentType;
import net.geant.nmaas.dcn.deployment.DcnRepositoryManager;
import net.geant.nmaas.dcn.deployment.entities.DcnInfo;
import net.geant.nmaas.dcn.deployment.entities.DcnSpec;
import net.geant.nmaas.orchestration.repositories.DomainTechDetailsRepository;
import net.geant.nmaas.portal.api.domain.DomainRequest;
import net.geant.nmaas.portal.api.domain.UserView;
import net.geant.nmaas.portal.api.exception.MissingElementException;
import net.geant.nmaas.portal.api.exception.ProcessingException;
import net.geant.nmaas.portal.exceptions.ObjectNotFoundException;
import net.geant.nmaas.portal.persistent.entity.ApplicationStatePerDomain;
import net.geant.nmaas.portal.persistent.entity.Domain;
import net.geant.nmaas.portal.persistent.entity.Role;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.persistent.entity.UserRole;
import net.geant.nmaas.portal.persistent.repositories.DomainRepository;
import net.geant.nmaas.portal.persistent.repositories.UserRoleRepository;
import net.geant.nmaas.portal.service.ApplicationStatePerDomainService;
import net.geant.nmaas.portal.service.DomainService;
import net.geant.nmaas.portal.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static net.geant.nmaas.portal.persistent.entity.Role.ROLE_GUEST;

@Service
@Slf4j
public class DomainServiceImpl implements DomainService {

	private static final String DOMAIN_NOT_FOUND_MESSAGE = "Domain not found";

	public interface CodenameValidator {
		boolean valid(String codename);
	}

	private final CodenameValidator validator;
	private final CodenameValidator namespaceValidator;
	private final DomainRepository domainRepository;
	private final DomainTechDetailsRepository domainTechDetailsRepository;
	private final UserService userService;
	private final UserRoleRepository userRoleRepository;
	private final DcnRepositoryManager dcnRepositoryManager;
	private final ModelMapper modelMapper;
	private final ApplicationStatePerDomainService applicationStatePerDomainService;

	@Value("${domain.global:GLOBAL}")
	String globalDomain;

	@Autowired
	public DomainServiceImpl(CodenameValidator validator,
							 @Qualifier("NamespaceValidator") CodenameValidator namespaceValidator,
							 DomainRepository domainRepository,
							 DomainTechDetailsRepository domainTechDetailsRepository,
							 UserService userService,
							 UserRoleRepository userRoleRepository,
							 DcnRepositoryManager dcnRepositoryManager,
							 ModelMapper modelMapper,
							 ApplicationStatePerDomainService applicationStatePerDomainService
	){
		this.validator = validator;
		this.namespaceValidator = namespaceValidator;
		this.domainRepository = domainRepository;
		this.domainTechDetailsRepository = domainTechDetailsRepository;
		this.userService = userService;
		this.userRoleRepository = userRoleRepository;
		this.dcnRepositoryManager = dcnRepositoryManager;
		this.modelMapper = modelMapper;
		this.applicationStatePerDomainService = applicationStatePerDomainService;
	}

	@Override
	public List<Domain> getDomains() {
		return domainRepository.findAll()
				.stream()
				.filter(domain -> !domain.isDeleted())
				.map(val -> {
			// TODO remove this debug log when no longer required
			log.debug("Domains groups - "+ val.getGroups().size());
			val.getGroups().stream().map(group -> {
				log.debug("Domain - " + val.getName() + " group -" + group.getName());
				return group;
			}).collect(Collectors.toList());
			return val;
		}).collect(Collectors.toList());
	}

	@Override
	public Page<Domain> getDomains(Pageable pageable) {
		return domainRepository.findAll(pageable);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Domain createGlobalDomain() {
		return getGlobalDomain().orElseGet(() -> createDomain(new DomainRequest(this.globalDomain, this.globalDomain.toLowerCase(), true)));
	}

	@Override
	public Optional<Domain> getGlobalDomain() {
		return domainRepository.findByName(globalDomain);
	}

	@Override
	public boolean existsDomain(String name) {
		checkParam(name);
		return domainRepository.existsByName(name);
	}

	@Override
	public boolean existsDomainByCodename(String codename) {
		checkParam(codename);
		return domainRepository.existsByCodename(codename);
	}

	@Override
	public boolean existsDomainByExternalServiceDomain(String externalServiceDomain) {
		return domainTechDetailsRepository.existsByExternalServiceDomain(externalServiceDomain);
	}

	@Override
	public Domain createDomain(DomainRequest request) {
		checkParam(request.getName());
		checkParam(request.getCodename());

		if (Optional.ofNullable(validator).map(v -> v.valid(request.getCodename())).filter(result -> result).isEmpty()) {
			throw new ProcessingException(String.format("Domain codename is not valid (%s / %s)", request.getName(), request.getCodename()));
		}
		checkArgument(!existsDomainByCodename(request.getCodename()),
				String.format("Domain codename is not unique (provided value: %s)", request.getCodename()));
		if (StringUtils.isEmpty(request.getDomainTechDetails().getKubernetesNamespace())) {
			request.getDomainTechDetails().setKubernetesNamespace(request.getCodename());
		}
		if (!namespaceValidator.valid(request.getDomainTechDetails().getKubernetesNamespace())) {
			throw new ProcessingException("Kubernetes namespace is not valid");
		}
		if (StringUtils.isEmpty(request.getDomainTechDetails().getKubernetesIngressClass())) {
			request.getDomainTechDetails().setKubernetesIngressClass(request.getCodename());
		}
		if (StringUtils.isNotEmpty(request.getDomainTechDetails().getExternalServiceDomain())) {
			checkArgument(!domainTechDetailsRepository.existsByExternalServiceDomain(
					request.getDomainTechDetails().getExternalServiceDomain()),
					String.format("External service domain is not unique (provided value: %s)", request.getDomainTechDetails().getExternalServiceDomain()));
		}
		this.setCodenames(request);
		try {
			List<ApplicationStatePerDomain> applicationStatePerDomainList = applicationStatePerDomainService.generateListOfDefaultApplicationStatesPerDomain();
			Domain newDomain = modelMapper.map(request, Domain.class);
			newDomain.setApplicationStatePerDomain(applicationStatePerDomainList);
			return domainRepository.save(newDomain);
		} catch(Exception ex) {
			throw new ProcessingException("Unable to create new domain with given name or codename.");
		}
	}

	private void setCodenames(DomainRequest request) {
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
	public void storeDcnInfo(DcnInfo dcnInfo) {
		this.dcnRepositoryManager.storeDcnInfo(dcnInfo);
	}

	@Override
    public void updateDcnInfo(String domain, DcnDeploymentType dcnDeploymentType) {
	    this.dcnRepositoryManager.updateDcnDeploymentType(domain, dcnDeploymentType);
    }

	@Override
	public Optional<Domain> findDomain(String name) {
		return domainRepository.findByName(name);
	}

	@Override
	public Optional<Domain> findDomain(Long id) {
		return domainRepository.findById(id);
	}

	@Override
	public Optional<Domain> findDomainByCodename(String codename) {
		return domainRepository.findByCodename(codename);
	}

	@Override
	public void updateDomain(Domain domain) {
		checkParam(domain);
		checkGlobal(domain);
		if (domain.getId() == null) {
			throw new ProcessingException("Cannot update domain. Domain not created previously?");
		}
		if (StringUtils.isEmpty(domain.getDomainTechDetails().getKubernetesNamespace())) {
			domain.getDomainTechDetails().setKubernetesNamespace(domain.getCodename());
		}
		if (!namespaceValidator.valid(domain.getDomainTechDetails().getKubernetesNamespace())) {
			throw new ProcessingException("Kubernetes namespace is not valid.");
		}
		domainRepository.save(domain);
	}

	@Override
	public Domain changeDcnConfiguredFlag(Long domainId, boolean dcnConfigured) {
		checkParams(domainId);
		Domain domain = findDomain(domainId).orElseThrow(() -> new MissingElementException(DOMAIN_NOT_FOUND_MESSAGE));
		checkGlobal(domain);
		domain.getDomainDcnDetails().setDcnConfigured(dcnConfigured);
		return domainRepository.save(domain);
	}

	@Override
	public void changeDomainState(Long domainId, boolean active) {
		checkParams(domainId);
		Domain domain = findDomain(domainId).orElseThrow(() -> new MissingElementException(DOMAIN_NOT_FOUND_MESSAGE));
		checkGlobal(domain);
		domain.setActive(active);
		domainRepository.save(domain);
	}

	@Override
	public boolean removeDomain(Long id) {
		return findDomain(id).map(toRemove -> {
				checkGlobal(toRemove);
				domainRepository.delete(toRemove);
				return true;
			}).orElse(false);
	}

	@Override
	public boolean softRemoveDomain(Long domainId) {
		return findDomain(domainId).map(domain -> {
			checkGlobal(domain);
			domain.setDeleted(true);
            domain.setName(domain.getName() + "_DELETED_" + OffsetDateTime.now());
			removeAllUsersFromDomain(domain);
            triggerApplicationsUninstall();
			domainRepository.save(domain);
			return true;
		}).orElse(false);
	}

	@Override
	public void removeAllUsersFromDomain(Domain domain) {
		getMembers(domain.getId()).forEach(member -> removePreviousRoleInDomain(domain, member));
	}

	private void triggerApplicationsUninstall() {

    }

    @Override
	public List<User> getMembers(Long id) {
		return userRoleRepository.findDomainMembers(id);
	}

	public void addMemberRole(Long domainId, Long userId, Role role) {
		checkParams(domainId, userId);
		checkParams(role);

		Domain domain = getDomain(domainId);
		User user = getUser(userId);

		if (userRoleRepository.findByDomainAndUserAndRole(domain, user, role) == null) {
			removePreviousRoleInDomain(domain, user);
			userRoleRepository.save(new UserRole(user, domain, role));
		}
	}

	@Override
	public void addGlobalGuestUserRoleIfMissing(Long userId) {
		Optional<Domain> globalDomainOptional = this.getGlobalDomain();
		if (globalDomainOptional.isPresent()) {
			Long globalId = globalDomainOptional.get().getId();
			try{
				if(this.getMemberRoles(globalId, userId).isEmpty()){
					this.addMemberRole(globalId, userId, ROLE_GUEST);
				}
			} catch(ObjectNotFoundException e) {
				throw new MissingElementException(e.getMessage());
			}
		}
	}

	private void removePreviousRoleInDomain(Domain domain, User user) {
		user.getRoles().stream()
				.filter(value -> value.getDomain().getId().equals(domain.getId()))
				.findAny()
				.ifPresent(value -> userRoleRepository.deleteBy(user.getId(), domain.getId(), value.getRole()));
	}

	private User getUser(Long userId) {
		return userService.findById(userId).orElseThrow(() -> new ObjectNotFoundException("User not found"));
	}

	private Domain getDomain(Long domainId) {
		return findDomain(domainId).orElseThrow(() -> new ObjectNotFoundException(DOMAIN_NOT_FOUND_MESSAGE));
	}

	@Override
	public void removeMemberRole(Long domainId, Long userId, Role role) {
		checkParams(domainId, userId);
		checkParams(role);
		userRoleRepository.deleteBy(userId, domainId, role);
	}

	@Override
	public void removeMember(Long domainId, Long userId) {
		checkParams(domainId, userId);
		userRoleRepository.deleteBy(userId, domainId);
	}

	@Override
	public Set<Role> getMemberRoles(Long domainId, Long userId) {
		checkParams(domainId, userId);
		return userRoleRepository.findRolesByDomainAndUser(domainId, userId);
	}

	@Override
	public User getMember(Long domainId, Long userId) {
		checkParams(domainId, userId);
		return userRoleRepository.findDomainMember(domainId, userId)
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
	public List<UserView> findUsersWithDomainAdminRole(String domain) {
		return this.userRoleRepository.findDomainMembers(domain).stream()
				.filter(user -> user.getRoles().stream().anyMatch(role -> role.getRole().name().equalsIgnoreCase(Role.ROLE_DOMAIN_ADMIN.name()) && role.getDomain().getCodename().equals(domain)))
				.map(user -> modelMapper.map(user, UserView.class))
				.collect(Collectors.toList());
	}

	@Override
	public Domain getAppStatesFromGroups(Domain domain) {
		log.error("fire group filter for domain " + domain.getName());
		if (domain.getGroups().isEmpty()) {
			return domain;
		}
		List<ApplicationStatePerDomain> result = new ArrayList<>();
		domain.getGroups().forEach( group -> {
			result.addAll(group.getApplicationStatePerDomain());
		});

		domain.setApplicationStatePerDomain(
				domain.getApplicationStatePerDomain().stream().map( app -> {
					List<ApplicationStatePerDomain> tmp = result.stream().filter( val -> val.getApplicationBase().equals(app.getApplicationBase())).collect(Collectors.toList());
					app.setEnabled(tmp.stream().anyMatch(ApplicationStatePerDomain::isEnabled));
					if(tmp.stream().map(ApplicationStatePerDomain::getPvStorageSizeLimit).max(Long::compareTo).isPresent()){
						app.setPvStorageSizeLimit(tmp.stream().map(ApplicationStatePerDomain::getPvStorageSizeLimit).max(Long::compareTo).get());
					}
					else {
						app.setPvStorageSizeLimit(app.getPvStorageSizeLimit());
					}
					return app;
				}).collect(Collectors.toList())
		);

		return domain;
	}

	protected void checkParam(String name) {
		if (StringUtils.isEmpty(name)) {
			throw new IllegalArgumentException("Name is null");
		}
	}

	protected void checkParam(Domain domain) {
		if (domain == null) {
			throw new IllegalArgumentException("Domain is null");
		}
	}

	private void checkParams(Long id) {
		if (id == null) {
			throw new IllegalArgumentException("id is null");
		}
	}

	private void checkParams(Role role) {
		if (role == null) {
			throw new IllegalArgumentException("role is null");
		}
	}

	private void checkParams(Long domainId, Long userId) {
		if (domainId == null) {
			throw new IllegalArgumentException("domainId is null");
		}
		if (userId == null) {
			throw new IllegalArgumentException("userId is null");
		}
	}

	private void checkGlobal(Domain domain){
		if (domain.getCodename().equals(globalDomain)) {
			throw new IllegalArgumentException("Global domain can't be updated or removed");
		}
	}

}