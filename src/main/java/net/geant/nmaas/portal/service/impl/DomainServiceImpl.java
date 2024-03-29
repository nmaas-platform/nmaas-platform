package net.geant.nmaas.portal.service.impl;

import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.dcn.deployment.DcnDeploymentType;
import net.geant.nmaas.dcn.deployment.DcnRepositoryManager;
import net.geant.nmaas.dcn.deployment.entities.DcnInfo;
import net.geant.nmaas.dcn.deployment.entities.DcnSpec;
import net.geant.nmaas.dcn.deployment.repositories.DomainDcnDetailsRepository;
import net.geant.nmaas.orchestration.repositories.DomainTechDetailsRepository;
import net.geant.nmaas.portal.api.domain.DomainAnnotationView;
import net.geant.nmaas.portal.api.domain.DomainGroupView;
import net.geant.nmaas.portal.api.domain.DomainRequest;
import net.geant.nmaas.portal.api.domain.KeyValueView;
import net.geant.nmaas.portal.api.domain.UserView;
import net.geant.nmaas.portal.api.exception.MissingElementException;
import net.geant.nmaas.portal.api.exception.ProcessingException;
import net.geant.nmaas.portal.events.DomainCreatedEvent;
import net.geant.nmaas.portal.exceptions.ObjectNotFoundException;
import net.geant.nmaas.portal.persistent.entity.ApplicationBase;
import net.geant.nmaas.portal.persistent.entity.ApplicationStatePerDomain;
import net.geant.nmaas.portal.persistent.entity.Domain;
import net.geant.nmaas.portal.persistent.entity.DomainAnnotation;
import net.geant.nmaas.portal.persistent.entity.DomainGroup;
import net.geant.nmaas.portal.persistent.entity.Role;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.persistent.entity.UserRole;
import net.geant.nmaas.portal.persistent.repositories.DomainAnnotationsRepository;
import net.geant.nmaas.portal.persistent.repositories.DomainRepository;
import net.geant.nmaas.portal.persistent.repositories.UserRoleRepository;
import net.geant.nmaas.portal.service.ApplicationStatePerDomainService;
import net.geant.nmaas.portal.service.DomainGroupService;
import net.geant.nmaas.portal.service.DomainService;
import net.geant.nmaas.portal.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
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
    private final DomainDcnDetailsRepository domainDcnDetailsRepository;
    private final DomainTechDetailsRepository domainTechDetailsRepository;
    private final UserService userService;
    private final UserRoleRepository userRoleRepository;
    private final DcnRepositoryManager dcnRepositoryManager;
    private final ModelMapper modelMapper;
    private final ApplicationStatePerDomainService applicationStatePerDomainService;
    private final DomainGroupService domainGroupService;
    private final ApplicationEventPublisher eventPublisher;
    private final DomainAnnotationsRepository domainAnnotationsRepository;

    @Value("${domain.global:GLOBAL}")
    String globalDomain;

    @Autowired
    public DomainServiceImpl(CodenameValidator validator,
                             @Qualifier("NamespaceValidator") CodenameValidator namespaceValidator,
                             DomainRepository domainRepository,
                             DomainDcnDetailsRepository domainDcnDetailsRepository,
                             DomainTechDetailsRepository domainTechDetailsRepository,
                             UserService userService,
                             UserRoleRepository userRoleRepository,
                             DcnRepositoryManager dcnRepositoryManager,
                             ModelMapper modelMapper,
                             ApplicationStatePerDomainService applicationStatePerDomainService,
                             DomainGroupService domainGroupService,
                             ApplicationEventPublisher eventPublisher,
                             DomainAnnotationsRepository domainAnnotationsRepository
    ) {
        this.validator = validator;
        this.namespaceValidator = namespaceValidator;
        this.domainRepository = domainRepository;
        this.domainDcnDetailsRepository = domainDcnDetailsRepository;
        this.domainTechDetailsRepository = domainTechDetailsRepository;
        this.userService = userService;
        this.userRoleRepository = userRoleRepository;
        this.dcnRepositoryManager = dcnRepositoryManager;
        this.modelMapper = modelMapper;
        this.applicationStatePerDomainService = applicationStatePerDomainService;
        this.domainGroupService = domainGroupService;
        this.eventPublisher = eventPublisher;
        this.domainAnnotationsRepository = domainAnnotationsRepository;
    }

    @Override
    public List<Domain> getDomains() {
        return domainRepository.findAll()
                .stream()
                .filter(domain -> !domain.isDeleted())
                .collect(Collectors.toList());
    }

    @Override
    public Page<Domain> getDomains(Pageable pageable) {
        return domainRepository.findAll(pageable);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Domain createGlobalDomain() {
        return getGlobalDomain()
                .orElseGet(() -> createDomain(new DomainRequest(this.globalDomain, this.globalDomain.toLowerCase(), true)));
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
            Domain saved = domainRepository.save(newDomain);
            if (!saved.getName().equals(globalDomain)) {
                eventPublisher.publishEvent(new DomainCreatedEvent(this, new DomainCreatedEvent.DomainSpec(saved.getId(), saved.getName(), saved.getCodename(), request.getAnnotations())));
            }
            return saved;
        } catch (Exception ex) {
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
            dcnRepositoryManager.removeDcnInfo(toRemove.getCodename());
            checkGlobal(toRemove);
            domainRepository.delete(toRemove);
            return true;
        }).orElse(false);
    }

    @Transactional
    @Override
    public boolean softRemoveDomain(Long domainId) {
        String removedSuffix = "_DELETED_" + OffsetDateTime.now();
        return findDomain(domainId).map(domain -> {
            checkGlobal(domain);
            dcnRepositoryManager.removeDcnInfo(domain.getCodename());
            domain.setDeleted(true);
            domain.setName(domain.getName() + removedSuffix);
            domain.setCodename(domain.getCodename() + removedSuffix);
            Long domainDcnDetailsId = domain.getDomainDcnDetails().getId();
            domain.setDomainDcnDetails(null);
            domainDcnDetailsRepository.deleteById(domainDcnDetailsId);
            Long domainTechDetailsId = domain.getDomainTechDetails().getId();
            domain.setDomainTechDetails(null);
            domainTechDetailsRepository.deleteById(domainTechDetailsId);
            removeAllUsersFromDomain(domain);
            removeDomainFromAllGroups(domain);
            domainRepository.save(domain);
            return true;
        }).orElse(false);
    }

    @Override
    public void removeDomainFromAllGroups(Domain domain) {
        List<Long> idsToDelete = domain.getGroups().stream().map(DomainGroup::getId).collect(Collectors.toList());
        idsToDelete.forEach(id -> {
            domainGroupService.deleteDomainFromGroup(domain, id);
        });
    }

    @Override
    public void removeAllUsersFromDomain(Domain domain) {
        getMembers(domain.getId()).forEach(member -> removePreviousRoleInDomain(domain, member));
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
            if (role != Role.ROLE_VL_DOMAIN_ADMIN) {
                removePreviousRoleInDomain(domain, user);
            }
            userRoleRepository.save(new UserRole(user, domain, role));
        }
    }

    @Override
    public void addGlobalGuestUserRoleIfMissing(Long userId) {
        Optional<Domain> globalDomainOptional = this.getGlobalDomain();
        if (globalDomainOptional.isPresent()) {
            Long globalId = globalDomainOptional.get().getId();
            try {
                if (this.getMemberRoles(globalId, userId).isEmpty()) {
                    this.addMemberRole(globalId, userId, ROLE_GUEST);
                }
            } catch (ObjectNotFoundException e) {
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
        if (domain.getGroups().isEmpty()) {
            return domain;
        }
        List<ApplicationStatePerDomain> result = new ArrayList<>();
        domain.getGroups().forEach(group -> result.addAll(group.getApplicationStatePerDomain()));

        domain.setApplicationStatePerDomain(
                domain.getApplicationStatePerDomain().stream().map(app -> {
                    List<ApplicationStatePerDomain> tmp = result.stream().filter(val -> val.getApplicationBase().equals(app.getApplicationBase())).collect(Collectors.toList());
                    app.setEnabled(tmp.stream().anyMatch(ApplicationStatePerDomain::isEnabled));
                    if (tmp.stream().map(ApplicationStatePerDomain::getPvStorageSizeLimit).max(Long::compareTo).isPresent()) {
                        app.setPvStorageSizeLimit(tmp.stream().map(ApplicationStatePerDomain::getPvStorageSizeLimit).max(Long::compareTo).get());
                    } else {
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

    private void checkGlobal(Domain domain) {
        if (domain.getCodename().equals(globalDomain)) {
            throw new IllegalArgumentException("Global domain can't be updated or removed");
        }
    }

    @Override
    public void checkDomainGroupUsers(DomainGroupView view) {
        List<Long> userToDelete = new ArrayList<>();
        DomainGroupView domainGroup = this.domainGroupService.getDomainGroup(view.getId());
        domainGroup.getManagers().forEach(user -> {
            if (view.getManagers().stream().noneMatch(viewUser -> viewUser.getId().equals(user.getId()))) {
                userToDelete.add(user.getId());
            }
        });
        userToDelete.forEach(userId -> {
            domainGroup.getDomains().forEach(domain -> {
                this.removeMemberRole(domain.getId(), userId, Role.ROLE_VL_DOMAIN_ADMIN);
            });
        });
    }

    @Override
    public void updateRolesInDomainGroupByUsers(DomainGroupView view) {
        view.getDomains().forEach(domain -> {
            view.getManagers().forEach(user -> {
                this.addMemberRole(domain.getId(), user.getId(), Role.ROLE_VL_DOMAIN_ADMIN);
            });
        });
    }

    // Domain annotations

    @Override
    public void addAnnotation(KeyValueView keyValue) {
        ModelMapper modelMapper = new ModelMapper();
        if (this.domainAnnotationsRepository.existsByKey(keyValue.getKey())) {
            throw new ProcessingException(String.format("Domain annotation with key (%s) already exist", keyValue.getKey()));
        }
        this.domainAnnotationsRepository.save(modelMapper.map(keyValue, DomainAnnotation.class));
    }

    @Override
    public boolean checkIfAnnotationExist(String key) {
        return this.domainAnnotationsRepository.existsByKey(key);
    }

    @Override
    public void deleteAnnotation(Long id) {
        Optional<DomainAnnotation> domainFromDb = this.domainAnnotationsRepository.findById(id);
        domainFromDb.ifPresent(this.domainAnnotationsRepository::delete);
    }

    @Override
    public List<DomainAnnotation> getAnnotations() {
        return this.domainAnnotationsRepository.findAll();
    }

    @Override
    public void updateAnnotation(Long id, DomainAnnotationView annotation) {
        Optional<DomainAnnotation> domainFromDb = this.domainAnnotationsRepository.findById(id);
        if (domainFromDb.isPresent() && id.equals(annotation.getId())) {
            DomainAnnotation domainAnnotation = domainFromDb.get();
            domainAnnotation.setKey(annotation.getKey());
            domainAnnotation.setValue(annotation.getValue());
            this.domainAnnotationsRepository.save(domainAnnotation);
        }
    }

    @Override
    public void removeAppBaseFromAllDomains(ApplicationBase base) {
        getDomains().forEach(domain -> removeFromDomain(base, domain));
    }

    private void removeFromDomain(ApplicationBase base, Domain domain) {
        domain.getApplicationStatePerDomain().removeIf(state -> state.getApplicationBase().equals(base));
    }

}
