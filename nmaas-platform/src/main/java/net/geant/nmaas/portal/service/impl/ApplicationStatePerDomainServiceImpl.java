package net.geant.nmaas.portal.service.impl;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ApplicationStatePerDomainServiceImpl implements ApplicationStatePerDomainService {

    private DomainRepository domainRepository;
    private ApplicationBaseRepository applicationBaseRepository;

    @Autowired
    ApplicationStatePerDomainServiceImpl(DomainRepository domainRepository, ApplicationBaseRepository applicationBaseRepository){
        this.domainRepository = domainRepository;
        this.applicationBaseRepository = applicationBaseRepository;
    }

    @Override
    public List<ApplicationStatePerDomain> generateListOfDefaultApplicationStatesPerDomain() {

        List<ApplicationStatePerDomain> list = this.applicationBaseRepository.findAll().stream().map(ApplicationStatePerDomain::new).collect(Collectors.toList());
        // TODO set defaults
        return list;
    }

    @Override
    public void updateAllDomainsWithNewApplicationBase(ApplicationBase applicationBase) {
        ApplicationStatePerDomain appState = new ApplicationStatePerDomain(applicationBase);
        appState.setEnabled(true);
        // TODO set defaults
        domainRepository.saveAll(domainRepository.findAll().stream().peek(domain -> domain.addApplicationState(appState)).collect(Collectors.toList()));
    }

    @Override
    public List<ApplicationStatePerDomain> updateDomain(DomainView changes) {
        Domain updatedDomain = this.domainRepository.getOne(changes.getId());
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
        Optional<ApplicationBase> appBase = this.applicationBaseRepository.findByName(application.getName());
        if(!appBase.isPresent()){
            throw new IllegalArgumentException("Application name not found");
        }
        return isApplicationEnabledInDomain(domain, appBase.get());
    }

    @Override
    public boolean isApplicationEnabledInDomain(Domain domain, ApplicationBase appBase) {
        for(ApplicationStatePerDomain a: domain.getApplicationStatePerDomain()) {
            if (a.getApplicationBase().getId().equals(appBase.getId())) {
                if(!a.isEnabled()){
                    return false;
                }
                break;
            }
        }
        return true;
    }

    @Override
    public boolean validateAppConfigurationAgainstState(AppConfigurationView appConfig, ApplicationStatePerDomain appState) {
        return appConfig.getStorageSpace() <= appState.getPvStorageSizeLimit();
    }
}
