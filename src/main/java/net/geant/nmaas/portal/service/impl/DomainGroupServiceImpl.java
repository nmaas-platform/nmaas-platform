package net.geant.nmaas.portal.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.portal.api.domain.ApplicationStatePerDomainView;
import net.geant.nmaas.portal.api.domain.DomainGroupView;
import net.geant.nmaas.portal.api.exception.MissingElementException;
import net.geant.nmaas.portal.api.exception.ProcessingException;
import net.geant.nmaas.portal.persistent.entity.ApplicationStatePerDomain;
import net.geant.nmaas.portal.persistent.entity.Domain;
import net.geant.nmaas.portal.persistent.entity.DomainGroup;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.persistent.repositories.DomainGroupRepository;
import net.geant.nmaas.portal.service.ApplicationStatePerDomainService;
import net.geant.nmaas.portal.service.DomainGroupService;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
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


    private final ModelMapper modelMapper;

    @Override
    public Boolean existDomainGroup(String name, String codeName) {
        if (domainGroupRepository.existsByName(name)) {
            return true;
        }
        return domainGroupRepository.existsByCodename(codeName);
    }

    @Override
    public DomainGroupView createDomainGroup(DomainGroupView domainGroup) {
        //validation
        checkParam(domainGroup);
        if (existDomainGroup(domainGroup.getName(), domainGroup.getCodename())) {
            throw new IllegalArgumentException("Domain group with given name or codename already exists");
        }
        //creation
        List<ApplicationStatePerDomain> applicationStatePerDomainList = applicationStatePerDomainService.generateListOfDefaultApplicationStatesPerDomainDisabled();
        DomainGroup domainGroupEntity = modelMapper.map(domainGroup, DomainGroup.class);
        domainGroupEntity.setApplicationStatePerDomain(applicationStatePerDomainList);
        domainGroupEntity = domainGroupRepository.save(domainGroupEntity);
        return modelMapper.map(domainGroupEntity, DomainGroupView.class);
    }

    @Override
    public DomainGroupView addDomainsToGroup(List<Domain> domains, String groupCodeName) {
        DomainGroup domainGroup = domainGroupRepository.findByCodename(groupCodeName).orElseThrow();
        domains.forEach( domain -> {
            log.debug("Adding domain {}/{} to group {}", domain.getName(), domain.getCodename(), groupCodeName);
            if (!domainGroup.getDomains().contains(domain)) {
                domainGroup.addDomain(domain);
            }
        });
        return modelMapper.map(domainGroupRepository.save(domainGroup), DomainGroupView.class);
    }

    @Override
    public DomainGroupView deleteDomainFromGroup(Domain domain, Long domainGroupId) {
        DomainGroup domainGroup = domainGroupRepository.findById(domainGroupId).orElseThrow();
        log.debug("Removing domain {} from group {}", domain.getCodename(), domainGroup.getCodename());
        domainGroup.removeDomain(domain);
        return modelMapper.map(domainGroupRepository.save(domainGroup), DomainGroupView.class);
    }

    @Override
    public void deleteDomainGroup(Long domainGroupId) {
        DomainGroup domainGroup = domainGroupRepository.findById(domainGroupId).orElseThrow();
        List<Domain> toRemove = new ArrayList<>(domainGroup.getDomains());
        Iterator<Domain> iterator = toRemove.iterator();
        while (iterator.hasNext()) {
            Domain domain = iterator.next();
            domain.getGroups().remove(domainGroup);
            deleteDomainFromGroup(domain, domainGroupId);
            iterator.remove();
        }
        domainGroupRepository.deleteById(domainGroupId);
    }

    @Override
    public DomainGroupView getDomainGroup(Long domainGroupId) {
        Optional<DomainGroup> domainGroup = this.domainGroupRepository.findById(domainGroupId);
        if (domainGroup.isPresent()) {
            return modelMapper.map(domainGroup.get(), DomainGroupView.class);
        } else {
            throw new MissingElementException("Domain group not found");
        }
    }

    @Override
    public List<DomainGroupView> getAllDomainGroups() {
        return domainGroupRepository.findAll().stream()
                .map(g -> modelMapper.map(g, DomainGroupView.class))
                .collect(Collectors.toList());
    }

    @Override
    public DomainGroupView updateDomainGroup(Long domainGroupId, DomainGroupView view) {
        if (!domainGroupId.equals(view.getId())) {
            throw new ProcessingException(String.format("Wrong domain group identifier (%s)", domainGroupId));
        }
        DomainGroup domainGroup = this.domainGroupRepository.findById(domainGroupId).orElseThrow();
//        updateRolesInDomainsByUsers(view);
        domainGroup.setCodename(view.getCodename());
        domainGroup.setName(view.getName());
        domainGroup.setManagers(view.getManagers().stream().map(user -> modelMapper.map(user, User.class)).collect(Collectors.toList()));
        for (ApplicationStatePerDomain appState: domainGroup.getApplicationStatePerDomain()) {
            for (ApplicationStatePerDomainView appStateView : view.getApplicationStatePerDomain()) {
                if (appState.getApplicationBase().getId().equals(appStateView.getApplicationBaseId())) {
                    appState.applyChangedState(appStateView);
                }
            }
        }

        domainGroupRepository.save(domainGroup);
        return modelMapper.map(domainGroup, DomainGroupView.class);
    }

    protected void checkParam(DomainGroupView domainGroup) {
        if (StringUtils.isEmpty(domainGroup.getName()) || StringUtils.isEmpty(domainGroup.getCodename())) {
            throw new IllegalArgumentException("Name is null or empty");
        }
    }

}