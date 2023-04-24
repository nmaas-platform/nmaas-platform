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
import net.geant.nmaas.portal.persistent.repositories.DomainGroupRepository;
import net.geant.nmaas.portal.service.ApplicationStatePerDomainService;
import net.geant.nmaas.portal.service.DomainGroupService;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DomainGroupServiceImpl implements DomainGroupService {

    private final DomainGroupRepository domainGroupRepository;
    private final ModelMapper modelMapper;

    private final ApplicationStatePerDomainService applicationStatePerDomainService;

    @Override
    public Boolean existDomainGroup(String name, String codeName) {
        if (this.domainGroupRepository.existsByName(name)) {
            return true;
        }
        return this.domainGroupRepository.existsByCodename(codeName);
    }

    @Override
    public DomainGroupView createDomainGroup(DomainGroupView domainGroup) {
        //validation
        checkParam(domainGroup);
        if (existDomainGroup(domainGroup.getName(), domainGroup.getCodename())) {
            throw new IllegalArgumentException("Domain group with given name or codename already exists");
        }
        //creation
        List<ApplicationStatePerDomain> applicationStatePerDomainList = applicationStatePerDomainService.generateListOfDefaultApplicationStatesPerDomain();
        DomainGroup domainGroupEntity = modelMapper.map(domainGroup, DomainGroup.class);
        domainGroupEntity.setApplicationStatePerDomain(applicationStatePerDomainList);
        domainGroupEntity = this.domainGroupRepository.save(domainGroupEntity);
        return modelMapper.map(domainGroupEntity, DomainGroupView.class);
    }

    @Override
    public DomainGroupView addDomainsToGroup(List<Domain> domains, String groupCodeName) {
        if (domainGroupRepository.findByCodename(groupCodeName).isEmpty()) {
            throw new MissingElementException("Domains group not found");
        }
        DomainGroup domainGroup = domainGroupRepository.findByCodename(groupCodeName).get();
        domains.forEach( domain -> {
            log.error("domain - "+ domain.toString());
            if(!domainGroup.getDomains().contains(domain)) {
                domainGroup.addDomain(domain);
            }
        });
        return modelMapper.map(domainGroupRepository.save(domainGroup), DomainGroupView.class);
    }

    @Override
    public DomainGroupView deleteDomainFromGroup(Domain domain, Long domainGroupId) {
        if (domainGroupRepository.findById(domainGroupId).isEmpty()){
            throw new MissingElementException("Domains group not found");
        }
        DomainGroup domainGroup = domainGroupRepository.findById(domainGroupId).get();
        domainGroup.removeDomain(domain);
        return modelMapper.map(domainGroupRepository.save(domainGroup), DomainGroupView.class);
    }

    @Override
    public void deleteDomainGroup(Long domainGroupId) {
        this.domainGroupRepository.deleteById(domainGroupId);
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
        return domainGroupRepository.findAll().stream().map(g -> modelMapper.map(g, DomainGroupView.class)).collect(Collectors.toList());
    }

    @Override
    public DomainGroupView updateDomainGroup(Long domainGroupId, DomainGroupView view) {
        if (!domainGroupId.equals(view.getId())) {
            throw new ProcessingException("Wrong domain group Id");
        }
        if (domainGroupRepository.findById(domainGroupId).isPresent()) {
            DomainGroup domainGroup = this.domainGroupRepository.findById(domainGroupId).get();
            domainGroup.setCodename(view.getCodename());
            domainGroup.setName(view.getName());
            for(ApplicationStatePerDomain appState: domainGroup.getApplicationStatePerDomain()){
                for(ApplicationStatePerDomainView appStateView : view.getApplicationStatePerDomain()) {
                    if (appState.getApplicationBase().getId().equals(appStateView.getApplicationBaseId())) {
                        appState.applyChangedState(appStateView);
                    }
                }
            }
            this.domainGroupRepository.save(domainGroup);
            return modelMapper.map(domainGroup, DomainGroupView.class);
        } else {
            throw new MissingElementException("Domain group not found");
        }
    }

    protected void checkParam(DomainGroupView domainGroup) {
        if (StringUtils.isEmpty(domainGroup.getName()) || StringUtils.isEmpty(domainGroup.getCodename())) {
            throw new IllegalArgumentException("Name is null");
        }
    }

}
