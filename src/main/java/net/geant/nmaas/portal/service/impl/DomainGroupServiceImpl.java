package net.geant.nmaas.portal.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.api.dto.applications.ApplicationStatePerDomainDto;
import net.geant.nmaas.api.dto.domains.DomainGroupBaseDto;
import net.geant.nmaas.api.dto.domains.DomainGroupDto;
import net.geant.nmaas.api.dto.users.RoleDto;
import net.geant.nmaas.api.dto.users.UserViewMinimal;
import net.geant.nmaas.portal.api.exceptions.MissingElementException;
import net.geant.nmaas.portal.api.exceptions.ProcessingException;
import net.geant.nmaas.portal.events.DomainGroupChangedEvent;
import net.geant.nmaas.portal.persistence.entity.ApplicationBase;
import net.geant.nmaas.portal.persistence.entity.ApplicationStatePerDomain;
import net.geant.nmaas.portal.persistence.entity.Domain;
import net.geant.nmaas.portal.persistence.entity.DomainGroup;
import net.geant.nmaas.portal.persistence.entity.Role;
import net.geant.nmaas.portal.persistence.entity.User;
import net.geant.nmaas.portal.persistence.repositories.DomainGroupRepository;
import net.geant.nmaas.portal.persistence.repositories.UserRoleRepository;
import net.geant.nmaas.portal.service.ApplicationStatePerDomainService;
import net.geant.nmaas.portal.service.DomainGroupService;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DomainGroupServiceImpl implements DomainGroupService {

    private final DomainGroupRepository domainGroupRepository;
    private final ApplicationStatePerDomainService applicationStatePerDomainService;
    private final ApplicationEventPublisher eventPublisher;
    private final UserRoleRepository userRoleRepository;

    private final ModelMapper modelMapper;

    @Override
    public Boolean existDomainGroup(String name, String codeName) {
        if (domainGroupRepository.existsByName(name)) {
            return true;
        }
        return domainGroupRepository.existsByCodename(codeName);
    }

    @Override
    public DomainGroupDto createDomainGroup(DomainGroupDto domainGroupDto) {
        //validation
        checkParam(domainGroupDto);
        if (existDomainGroup(domainGroupDto.getName(), domainGroupDto.getCodename())) {
            throw new IllegalArgumentException("Domain group with given name or codename already exists");
        }
        if (!domainGroupDto.getManagers().isEmpty()) {
            List<UserViewMinimal> validateManagers = new ArrayList<>();
            domainGroupDto.getManagers().forEach(manager -> {
                if (manager.getRoles().stream().noneMatch(
                        role -> role.getRole() == RoleDto.ROLE_SYSTEM_ADMIN)) {
                    validateManagers.add(manager);
                }
            });
            domainGroupDto.setManagers(validateManagers);
        }
        //creation
        List<ApplicationStatePerDomain> applicationStatePerDomainList = applicationStatePerDomainService.generateListOfDefaultApplicationStatesPerDomainDisabled();
        DomainGroup domainGroupEntity = modelMapper.map(domainGroupDto, DomainGroup.class);
        domainGroupEntity.setApplicationStatePerDomain(applicationStatePerDomainList);
        domainGroupEntity = domainGroupRepository.save(domainGroupEntity);

        DomainGroupDto domainGroupView = modelMapper.map(domainGroupEntity, DomainGroupDto.class);
        eventPublisher.publishEvent(new DomainGroupChangedEvent(this, "create", domainGroupView));
        return domainGroupView;
    }

    @Override
    public DomainGroupDto addDomainsToGroup(List<Domain> domains, Long domainGroupId) {
        DomainGroup domainGroup = domainGroupRepository.findById(domainGroupId).orElseThrow();
        domains.forEach(domain -> {
            logDomainAddSummary(domain, domainGroup.getCodename());
            if (!domainGroup.getDomains().contains(domain)) {
                domainGroup.addDomain(domain);
            }
        });
        return modelMapper.map(domainGroupRepository.save(domainGroup), DomainGroupDto.class);
    }

    private static void logDomainAddSummary(Domain domain, String domainGroup) {
        log.debug("Adding domain {}/{} to group {}", domain.getName(), domain.getCodename(), domainGroup);
    }

    @Override
    public DomainGroupDto addDomainsToGroup(List<Domain> domains, String groupCodeName) {
        DomainGroup domainGroup = domainGroupRepository.findByCodename(groupCodeName).orElseThrow();
        domains.forEach(domain -> {
            logDomainAddSummary(domain, groupCodeName);
            if (!domainGroup.getDomains().contains(domain)) {
                domainGroup.addDomain(domain);
            }
        });
        return modelMapper.map(domainGroupRepository.save(domainGroup), DomainGroupDto.class);
    }

    @Override
    public DomainGroupDto deleteDomainFromGroup(Domain domain, Long domainGroupId) {
        DomainGroup domainGroup = domainGroupRepository.findById(domainGroupId).orElseThrow();
        log.debug("Removing domain {} from group {}", domain.getCodename(), domainGroup.getCodename());
        domainGroup.removeDomain(domain);
        domainGroup.getManagers().forEach(manager ->
                userRoleRepository.deleteBy(manager.getId(), domain.getId(), Role.ROLE_GROUP_DOMAIN_ADMIN)
        );
        return modelMapper.map(domainGroupRepository.save(domainGroup), DomainGroupDto.class);
    }

    @Override
    public void deleteDomainGroup(Long domainGroupId) {
        DomainGroup domainGroup = domainGroupRepository.findById(domainGroupId).orElseThrow();
        DomainGroupDto domainGroupView = modelMapper.map(domainGroup, DomainGroupDto.class);
        List<Domain> toRemove = new ArrayList<>(domainGroup.getDomains());
        Iterator<Domain> iterator = toRemove.iterator();
        while (iterator.hasNext()) {
            Domain domain = iterator.next();
            domain.getGroups().remove(domainGroup);
            deleteDomainFromGroup(domain, domainGroupId);
            iterator.remove();
        }
        domainGroupRepository.deleteById(domainGroupId);
        eventPublisher.publishEvent(new DomainGroupChangedEvent(this, "delete", domainGroupView));
    }

    @Override
    public DomainGroupDto getDomainGroup(Long domainGroupId) {
        Optional<DomainGroup> domainGroup = this.domainGroupRepository.findById(domainGroupId);
        if (domainGroup.isPresent()) {
            return modelMapper.map(domainGroup.get(), DomainGroupDto.class);
        } else {
            throw new MissingElementException("Domain group not found");
        }
    }

    @Override
    public List<DomainGroupBaseDto> getAllDomainGroups(String search) {
        return domainGroupRepository.findAllWithSearch(search).stream()
                .map(g -> modelMapper.map(g, DomainGroupBaseDto.class))
                .toList();
    }

    @Override
    public List<DomainGroupBaseDto> getAllDomainGroupsWhereManagerIsMember(User manager, String search) {
        return domainGroupRepository.findAllByManagersWithSearch(manager, search).stream()
                .map(g -> modelMapper.map(g, DomainGroupBaseDto.class)).toList();
    }

    @Override
    public List<DomainGroupDto> getAllDetailedDomainGroups(String search) {
        return domainGroupRepository.findAllWithSearch(search).stream()
                .map(g -> modelMapper.map(g, DomainGroupDto.class))
                .toList();
    }

    @Override
    public List<DomainGroupDto> getAllDetailedDomainGroupsWhereManagerIsMember(User manager, String search) {
        return domainGroupRepository.findAllByManagersWithSearch(manager, search).stream()
                .map(g -> modelMapper.map(g, DomainGroupDto.class))
                .toList();
    }

    @Override
    public Page<DomainGroupBaseDto> getPageableAllDomainGroupsAndSearch(Pageable pageable, String search) {
        return domainGroupRepository.getAllBaseDtoWithSearch(search, pageable);
    }

    @Override
    public Page<DomainGroupBaseDto> getPageableAllDomainGroupsWhereManagerIsMemberAndSearch(Pageable pageable, User manager, String search) {
        return domainGroupRepository.getAllBaseDtoByManagerWithSearch(manager, search, pageable);
    }

    @Override
    public DomainGroupDto updateDomainGroup(Long domainGroupId, DomainGroupDto view) {
        if (!domainGroupId.equals(view.getId())) {
            throw new ProcessingException(String.format("Wrong domain group identifier (%s)", domainGroupId));
        }
        DomainGroup domainGroup = this.domainGroupRepository.findById(domainGroupId).orElseThrow();
        // updateRolesInDomainsByUsers(view);
        domainGroup.setCodename(view.getCodename());
        domainGroup.setName(view.getName());
        domainGroup.setManagers(view.getManagers().stream()
                .map(user -> modelMapper.map(user, User.class))
                .collect(Collectors.toCollection(ArrayList::new))
        );
        for (ApplicationStatePerDomain appState : domainGroup.getApplicationStatePerDomain()) {
            for (ApplicationStatePerDomainDto appStateView : view.getApplicationStatePerDomain()) {
                if (appState.getApplicationBase().getId().equals(appStateView.getApplicationBaseId())) {
                    appState.applyChangedState(appStateView);
                }
            }
        }

        domainGroupRepository.save(domainGroup);

        DomainGroupDto domainGroupView = modelMapper.map(domainGroup, DomainGroupDto.class);
        eventPublisher.publishEvent(new DomainGroupChangedEvent(this, "update", domainGroupView));
        return domainGroupView;
    }

    protected void checkParam(DomainGroupDto domainGroup) {
        if (StringUtils.isEmpty(domainGroup.getName()) || StringUtils.isEmpty(domainGroup.getCodename())) {
            throw new IllegalArgumentException("Name is null or empty");
        }
    }

    public void deleteAppBaseFromAllAppState(ApplicationBase base) {
        domainGroupRepository.findAll().forEach(d ->
                d.getApplicationStatePerDomain().removeIf(state -> state.getApplicationBase().equals(base))
        );
    }

    @Override
    public void deleteUserFromAllDomainsGroups(User user) {
        domainGroupRepository.findAll().forEach(d ->
                d.getManagers().remove(user)
        );
    }

}
