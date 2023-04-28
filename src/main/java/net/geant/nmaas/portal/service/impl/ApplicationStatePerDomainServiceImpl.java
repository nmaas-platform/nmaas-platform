package net.geant.nmaas.portal.service.impl;

import lombok.RequiredArgsConstructor;
import net.geant.nmaas.orchestration.api.model.AppConfigurationView;
import net.geant.nmaas.portal.api.domain.ApplicationStatePerDomainView;
import net.geant.nmaas.portal.api.domain.DomainView;
import net.geant.nmaas.portal.persistent.entity.Application;
import net.geant.nmaas.portal.persistent.entity.ApplicationBase;
import net.geant.nmaas.portal.persistent.entity.ApplicationStatePerDomain;
import net.geant.nmaas.portal.persistent.entity.Domain;
import net.geant.nmaas.portal.persistent.repositories.ApplicationBaseRepository;
import net.geant.nmaas.portal.persistent.repositories.DomainRepository;
import net.geant.nmaas.portal.service.ApplicationStatePerDomainService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApplicationStatePerDomainServiceImpl implements ApplicationStatePerDomainService {

    public static final long DEFAULT_PV_STORAGE_SIZE_LIMIT = 20;

    private final DomainRepository domainRepository;
    private final ApplicationBaseRepository applicationBaseRepository;

    @Override
    public List<ApplicationStatePerDomain> generateListOfDefaultApplicationStatesPerDomain() {
        return applicationBaseRepository.findAll().stream()
                .map(appBase -> {
                    ApplicationStatePerDomain appState = new ApplicationStatePerDomain(appBase);
                    appState.setPvStorageSizeLimit(ApplicationStatePerDomainServiceImpl.DEFAULT_PV_STORAGE_SIZE_LIMIT);
                    return appState;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<Domain> updateAllDomainsWithNewApplicationBase(ApplicationBase applicationBase) {
        ApplicationStatePerDomain appState = new ApplicationStatePerDomain(applicationBase);
        appState.setEnabled(true);
        appState.setPvStorageSizeLimit(ApplicationStatePerDomainServiceImpl.DEFAULT_PV_STORAGE_SIZE_LIMIT);
        List<Domain> allDomains = domainRepository.findAll();
        allDomains.forEach(domain -> domain.addApplicationState(appState));
        return domainRepository.saveAll(allDomains);
    }

    @Override
    public List<ApplicationStatePerDomain> generateListOfDefaultApplicationStatesPerDomainDisabled() {
        return applicationBaseRepository.findAll().stream()
                .map(appBase -> {
                    ApplicationStatePerDomain appState = new ApplicationStatePerDomain(appBase);
                    appState.setPvStorageSizeLimit(ApplicationStatePerDomainServiceImpl.DEFAULT_PV_STORAGE_SIZE_LIMIT);
                    appState.setEnabled(false);
                    return appState;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ApplicationStatePerDomain> updateDomain(DomainView changes) {
        Domain updatedDomain = domainRepository.getReferenceById(changes.getId());
        List<ApplicationStatePerDomain> list = updatedDomain.getApplicationStatePerDomain();
        for(ApplicationStatePerDomain appState: list){
            for(ApplicationStatePerDomainView appStateView: changes.getApplicationStatePerDomain()){
                if(appState.getApplicationBase().getId().equals(appStateView.getApplicationBaseId())){
                    // rewrite state
                    appState.applyChangedState(appStateView);
                }
            }
        }
        return list;
    }

    @Override
    public boolean isApplicationEnabledInDomain(Domain domain, Application application) {
        Optional<ApplicationBase> appBase = applicationBaseRepository.findByName(application.getName());
        if (appBase.isEmpty()) {
            throw new IllegalArgumentException("Application with given name not found");
        }
        return isApplicationEnabledInDomain(domain, appBase.get());
    }

    @Override
    public boolean isApplicationEnabledInDomain(Domain domain, ApplicationBase appBase) {
        for(ApplicationStatePerDomain a: domain.getApplicationStatePerDomain()) {
            if (a.getApplicationBase().getId().equals(appBase.getId())) {
                if (a.isEnabled()) {
                    return true;
                }
                break;
            }
        }
        return false;
    }

    // in some cases the storage space may be set at the later stage (if not set by the user)
    @Override
    public boolean validateAppConfigurationAgainstState(AppConfigurationView appConfig, ApplicationStatePerDomain appState) {
        return appConfig.getStorageSpace() == null || appConfig.getStorageSpace() <= appState.getPvStorageSizeLimit();
    }

}
